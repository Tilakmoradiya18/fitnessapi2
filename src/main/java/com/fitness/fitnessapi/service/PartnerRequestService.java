package com.fitness.fitnessapi.service;

import com.fitness.fitnessapi.dto.*;
import com.fitness.fitnessapi.entity.PartnerRequest;
import com.fitness.fitnessapi.entity.TimeSlot;
import com.fitness.fitnessapi.entity.User;
import com.fitness.fitnessapi.enums.RequestStatus;
import com.fitness.fitnessapi.repository.PartnerRequestRepository;
import com.fitness.fitnessapi.repository.TimeSlotRepository;
import com.fitness.fitnessapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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


//    public ApiSuccessResponse getAvailableSlotResponse(Long partnerId) {
//        List<TimeSlot> slots = timeSlotRepository.findByUserIdAndIsBookedFalseAndIsExpiredFalse(partnerId);
//
//        List<TimeSlotResponseDTO> dtoList = slots.stream()
//                .map(slot -> new TimeSlotResponseDTO(
//                        slot.getId(),
//                        slot.getStartTime().toString(),
//                        slot.getEndTime().toString()
//                ))
//                .collect(Collectors.toList());
//
//        return new ApiSuccessResponse(
//                LocalDateTime.now(),
//                200,
//                "slots fetched successfully",
//                Map.of("slots", dtoList)
//        );
//    }

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

//
//    public ApiSuccessResponse getUpcomingRequestsForPartner(Long partnerId) {
//        List<PartnerRequest> requests = requestRepository
//                .findByReceiverIdAndStatusIn(partnerId, List.of(RequestStatus.PENDING, RequestStatus.ACCEPTED));
//
//        LocalDate today = LocalDate.now();
//
//        List<PartnerRequestResponseDTO> responseList = requests.stream()
//                .filter(req -> {
//                    TimeSlot slot = req.getSlot();
//                    // Include only non-expired and today or future dates
//                    return !slot.isExpired() && !slot.getDate().isBefore(today);
//                })
//                .map(req -> new PartnerRequestResponseDTO(
//                        req.getSender().getId(),
//                        req.getSender().getName(),
//                        new SlotInfoDTO(
//                                req.getSlot().getId(),
//                                req.getSlot().getStartTime().toString(),
//                                req.getSlot().getEndTime().toString(),
//                                req.getSlot().getDate().toString()
//                        ),
//                        req.getStatus().name()
//                ))
//                .collect(Collectors.toList());
//
//        return new ApiSuccessResponse(
//                LocalDateTime.now(),
//                200,
//                "Upcoming requests fetched successfully.",
//                Map.of("requests", responseList)
//        );
//    }

    public ApiSuccessResponse getUpcomingRequestsForPartner(Long partnerId) {
        List<PartnerRequest> requests = requestRepository
                .findByReceiverIdAndStatusIn(partnerId, List.of(RequestStatus.PENDING, RequestStatus.ACCEPTED));

        LocalDate today = LocalDate.now();

        List<PartnerRequestResponseDTO> responseList = requests.stream()
                .filter(req -> {
                    TimeSlot slot = req.getSlot();
                    return !slot.isExpired() && !slot.getDate().isBefore(today);
                })
                .map(req -> new PartnerRequestResponseDTO(
                        req.getId(),  // 👈 include requestId here
                        req.getSender().getId(),
                        req.getSender().getName(),
                        new SlotInfoDTO(
                                req.getSlot().getId(),
                                req.getSlot().getStartTime().toString(),
                                req.getSlot().getEndTime().toString(),
                                req.getSlot().getDate().toString()
                        ),
                        req.getStatus().name()
                ))
                .collect(Collectors.toList());

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Upcoming requests fetched successfully.",
                Map.of("requests", responseList)
        );
    }



    public ApiSuccessResponse acceptRequest(Long receiverId, Long requestId) {
        PartnerRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new RuntimeException("Unauthorized action");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request is already handled");
        }

        // Mark request as accepted
        request.setStatus(RequestStatus.ACCEPTED);
        requestRepository.save(request);

        // Mark slot as booked
        TimeSlot slot = request.getSlot();
        slot.setBooked(true);
        timeSlotRepository.save(slot);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Request accepted successfully.",
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


}
