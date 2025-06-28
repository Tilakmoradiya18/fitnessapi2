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



    public ApiSuccessResponse getUpcomingRequestsForPartner(Long partnerId) {
        List<PartnerRequest> requests = requestRepository
                .findByReceiverIdAndStatusIn(partnerId, List.of(RequestStatus.PENDING, RequestStatus.ACCEPTED));

        List<PartnerRequestResponseDTO> responseList = requests.stream()
                .map(req -> new PartnerRequestResponseDTO(
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



}
