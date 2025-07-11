package com.fitness.fitnessapi.service;

import com.fitness.fitnessapi.dto.*;
import com.fitness.fitnessapi.entity.*;
import com.fitness.fitnessapi.enums.RequestStatus;
import com.fitness.fitnessapi.enums.TransactionType;
import com.fitness.fitnessapi.repository.*;
import com.fitness.fitnessapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class PartnerRequestService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private RatePerHourRepository ratePerHourRepository;

    @Autowired
    private PartnerRequestRepository requestRepository;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private JwtUtil jwtUtil;



    public ApiSuccessResponse sendRequest(Long senderId, PartnerRequestDTO dto) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        LocalTime startTime = LocalTime.parse(dto.getStartTime());
        LocalTime endTime = LocalTime.parse(dto.getEndTime());

        TimeSlot slot = timeSlotRepository
                .findByUserIdAndStartTimeAndEndTimeAndIsBookedFalseAndIsExpiredFalse(
                        receiver.getId(), startTime, endTime
                ).orElseThrow(() -> new RuntimeException("Requested time slot is not available."));

        Optional<PartnerRequest> existing = requestRepository
                .findBySenderIdAndSlotIdAndStatus(senderId, slot.getId(), RequestStatus.PENDING);

        if (existing.isPresent()) {
            throw new RuntimeException("You have already sent a request for this slot.");
        }

        PartnerRequest request = new PartnerRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setSlot(slot);
        request.setStatus(RequestStatus.PENDING);
        request.setRequestedAt(LocalDateTime.now());

        requestRepository.save(request);

        // ✅ Return proper ApiSuccessResponse
        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Request sent successfully.",
                null
        );
    }


    public ApiSuccessResponse getAvailableSlotResponse(Long partnerId) {
        List<TimeSlot> slots = timeSlotRepository
                .findByUserIdAndIsBookedFalseAndIsExpiredFalseAndIsDeletedFalse(partnerId);

        List<TimeSlotResponseDTO> dtoList = slots.stream()
                .map(slot -> new TimeSlotResponseDTO(
                        slot.getId(),
                        slot.getStartTime().toString(),
                        slot.getEndTime().toString()
                ))
                .collect(Collectors.toList());

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "slots fetched successfully",
                Map.of("slots", dtoList)
        );
    }

    
    public ApiSuccessResponse getUpcomingRequestsForPartner(Long partnerId) {
        List<PartnerRequest> requests = requestRepository
                .findByReceiverIdAndStatusIn(partnerId, List.of(RequestStatus.PENDING, RequestStatus.ACCEPTED,RequestStatus.REJECTED));

        LocalDate today = LocalDate.now();

        List<PartnerRequestResponseDTO> responseList = requests.stream()
                .filter(req -> {
                    TimeSlot slot = req.getSlot();
                    return !slot.isExpired() && !slot.getDate().isBefore(today);
                })
                .map(req -> {
                    Long senderId = req.getSender().getId();

                    // Fetch hourly rate from rate_per_hour table
                    Double hourlyRate = ratePerHourRepository.findByUserId(senderId)
                            .map(RatePerHour::getPrice)
                            .orElse(null); // or .orElse(0.0) if you want default

                    return new PartnerRequestResponseDTO(
                            req.getId(),
                            senderId,
                            req.getSender().getName(),
                            new SlotInfoDTO(
                                    req.getSlot().getId(),
                                    req.getSlot().getStartTime().toString(),
                                    req.getSlot().getEndTime().toString(),
                                    req.getSlot().getDate().toString()
                            ),
                            req.getStatus().name(),
                            hourlyRate // added hourly rate
                    );
                })
                .collect(Collectors.toList());

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Upcoming requests fetched successfully.",
                Map.of("requests", responseList)
        );
    }


    @Transactional
    public ApiSuccessResponse acceptRequest(Long receiverId, Long requestId) {
        PartnerRequest acceptedRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!acceptedRequest.getReceiver().getId().equals(receiverId)) {
            throw new RuntimeException("Unauthorized action");
        }

        if (acceptedRequest.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request is already handled");
        }

        acceptedRequest.setStatus(RequestStatus.ACCEPTED);
        requestRepository.save(acceptedRequest);

        TimeSlot slot = acceptedRequest.getSlot();
        slot.setBooked(true);
        timeSlotRepository.save(slot);

        // Auto-cancel other conflicting requests (keep this logic as is)
        List<PartnerRequest> conflicts = requestRepository
                .findBySenderIdAndSlotDateAndStatus(acceptedRequest.getSender().getId(), slot.getDate(), RequestStatus.PENDING);
        for (PartnerRequest r : conflicts) {
            if (!r.getId().equals(requestId) &&
                    r.getSlot().getStartTime().equals(slot.getStartTime()) &&
                    r.getSlot().getEndTime().equals(slot.getEndTime())) {
                r.setStatus(RequestStatus.AUTO_CANCELLED);
                requestRepository.save(r);
            }
        }

        // ✅ PAYMENT BLOCK LOGIC START
        User sender = acceptedRequest.getSender();
        User receiver = acceptedRequest.getReceiver();

        int durationInMinutes = (int) Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
        double hours = durationInMinutes / 60.0;

        double hourlyRate = ratePerHourRepository.findByUser(receiver)
                .map(RatePerHour::getPrice)
                .orElse(0.0);
        double amountToBlock = hours * hourlyRate;

        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

        if (senderWallet.getBalance() < amountToBlock) {
            throw new RuntimeException("Insufficient balance.");
        }

        senderWallet.setBalance(senderWallet.getBalance() - amountToBlock);
        senderWallet.setLastUpdate(LocalDateTime.now());
        walletRepository.save(senderWallet);

        // ✅ Store a new BLOCKED transaction
        String otp = String.format("%04d", new Random().nextInt(10000)); // e.g. 9347

        TransactionHistory txn = new TransactionHistory();
        txn.setUser(sender);
        txn.setReferenceUser(receiver);
        txn.setAmount(amountToBlock);
        txn.setType(TransactionType.BLOCKED);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setOtp(otp);
        txn.setCompleted(false);
        transactionHistoryRepository.save(txn);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Request accepted and amount blocked successfully.",
                Map.of(
                        "transactionId", txn.getId(),
                        "blockedAmount", amountToBlock,
                        "otp", otp
                )
        );
    }



    public ApiSuccessResponse rejectRequest(Long receiverId, Long requestId) {
        PartnerRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new RuntimeException("Unauthorized action");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request is already handled");
        }

        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Request rejected successfully.",
                null
        );
    }


    public ApiSuccessResponse getSentRequestStatus(Long senderId) {
        List<PartnerRequest> requests = requestRepository.findBySenderId(senderId);

        List<SentRequestStatusDTO> responseList = requests.stream()
                .map(req -> {
                    Long receiverId = req.getReceiver().getId();
                    String receiverName = req.getReceiver().getName();

                    // Fetch hourly rate for each receiver
                    Double hourlyRate = ratePerHourRepository.findByUserId(receiverId)
                            .map(RatePerHour::getPrice)
                            .orElse(null); // or .orElse(0.0)

                    return new SentRequestStatusDTO(
                            req.getId(),
                            receiverId,
                            receiverName,
                            new SlotInfoDTO(
                                    req.getSlot().getId(),
                                    req.getSlot().getStartTime().toString(),
                                    req.getSlot().getEndTime().toString(),
                                    req.getSlot().getDate().toString()
                            ),
                            req.getStatus().name(),
                            hourlyRate
                    );
                })
                .collect(Collectors.toList());

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Sent requests fetched successfully.",
                Map.of("requests", responseList)
        );
    }


    @Scheduled(cron = "0 * * * * *") // Runs every hour
    @Transactional
    public void autoCancelExpiredRequests() {
        List<PartnerRequest> pendingRequests = requestRepository.findByStatus(RequestStatus.PENDING);

        for (PartnerRequest request : pendingRequests) {
            TimeSlot slot = request.getSlot();
            if (slot.isExpired()) {
                request.setStatus(RequestStatus.AUTO_CANCELLED);
            }
        }

        requestRepository.saveAll(pendingRequests);
        System.out.println("✅ Auto-cancelled expired pending partner requests");
    }

    public ApiSuccessResponse cancelSentRequest(Long requestId, HttpServletRequest httpRequest) {
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PartnerRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getSender().getId().equals(sender.getId())) {
            throw new RuntimeException("Unauthorized cancellation");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Cannot cancel request after it is accepted, rejected, or expired.");
        }

        request.setStatus(RequestStatus.CANCELLED);
        requestRepository.save(request);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Request cancelled successfully.",
                null
        );
    }


}
