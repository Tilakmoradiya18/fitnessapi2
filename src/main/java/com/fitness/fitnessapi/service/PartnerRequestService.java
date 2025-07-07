package com.fitness.fitnessapi.service;

import com.fitness.fitnessapi.dto.*;
import com.fitness.fitnessapi.entity.PartnerRequest;
import com.fitness.fitnessapi.entity.RatePerHour;
import com.fitness.fitnessapi.entity.TimeSlot;
import com.fitness.fitnessapi.entity.User;
import com.fitness.fitnessapi.enums.RequestStatus;
import com.fitness.fitnessapi.repository.PartnerRequestRepository;
import com.fitness.fitnessapi.repository.RatePerHourRepository;
import com.fitness.fitnessapi.repository.TimeSlotRepository;
import com.fitness.fitnessapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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


    public ApiSuccessResponse acceptRequest(Long receiverId, Long requestId) {
        PartnerRequest acceptedRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!acceptedRequest.getReceiver().getId().equals(receiverId)) {
            throw new RuntimeException("Unauthorized action");
        }

        if (acceptedRequest.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request is already handled");
        }

        // ✅ Mark accepted request
        acceptedRequest.setStatus(RequestStatus.ACCEPTED);
        requestRepository.save(acceptedRequest);

        // ✅ Mark its slot as booked
        TimeSlot acceptedSlot = acceptedRequest.getSlot();
        acceptedSlot.setBooked(true);
        timeSlotRepository.save(acceptedSlot);

        // ✅ Auto-cancel other requests for same sender on same date with same time
        List<PartnerRequest> conflictingRequests = requestRepository
                .findBySenderIdAndSlotDateAndStatus(acceptedRequest.getSender().getId(), acceptedSlot.getDate(), RequestStatus.PENDING);

        for (PartnerRequest req : conflictingRequests) {
            TimeSlot slot = req.getSlot();

            boolean isSameTime = slot.getStartTime().equals(acceptedSlot.getStartTime()) &&
                    slot.getEndTime().equals(acceptedSlot.getEndTime());

            if (!req.getId().equals(acceptedRequest.getId()) && isSameTime) {
                req.setStatus(RequestStatus.AUTO_CANCELLED);
                requestRepository.save(req);
            }
        }

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Request accepted successfully. Conflicting requests auto-cancelled.",
                null
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



}
