//package com.fitness.fitnessapi.service;
//
//import com.fitness.fitnessapi.dto.TimeSlotRequest;
//import com.fitness.fitnessapi.dto.AvailabilityRequest;
//import com.fitness.fitnessapi.entity.*;
//import com.fitness.fitnessapi.repository.*;
//import com.fitness.fitnessapi.util.JwtUtil;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class AvailabilityService {
//
//        private TimeSlotRepository timeSlotRepository;
//        private RatePerHourRepository ratePerHourRepository;
//        private UserRepository userRepository;
//        private JwtUtil jwtUtil;
//
//    public void addTimeSlot(TimeSlotRequest request, HttpServletRequest httpRequest) {
//        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//        TimeSlot slot = TimeSlot.builder()
//                .user(user)
//                .date(request.getDate())
//                .startTime(request.getStartTime())
//                .endTime(request.getEndTime())
//                .isAvailableToday(false) // initially false
//                .build();
//
//        TimeSlot savedSlot = timeSlotRepository.save(slot);
//
//        RatePerHour rate = RatePerHour.builder()
//                .user(user)
//                .timeSlot(savedSlot)
//                .price(request.getHourlyRate())
//                .build();
//
//        ratePerHourRepository.save(rate);
//    }
//
//    public void toggleAvailability(AvailabilityRequest request, HttpServletRequest httpRequest) {
//        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//        List<TimeSlot> slots = timeSlotRepository.findByUserAndDate(user, request.getDate());
//        for (TimeSlot slot : slots) {
//            slot.setAvailableToday(request.isAvailable());
//        }
//
//        timeSlotRepository.saveAll(slots);
//    }
//}

package com.fitness.fitnessapi.service;

import com.fitness.fitnessapi.dto.ApiSuccessResponse;
import com.fitness.fitnessapi.dto.TimeSlotRequest;
import com.fitness.fitnessapi.dto.AvailabilityRequest;
import com.fitness.fitnessapi.entity.*;
import com.fitness.fitnessapi.repository.*;
import com.fitness.fitnessapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private RatePerHourRepository ratePerHourRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public ApiSuccessResponse addTimeSlot(TimeSlotRequest request, HttpServletRequest httpRequest) {
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Manually create TimeSlot
        TimeSlot slot = new TimeSlot();
        slot.setUser(user);
        slot.setDate(request.getDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setAvailableToday(false); // initially false

        TimeSlot savedSlot = timeSlotRepository.save(slot);

        // Manually create RatePerHour
        RatePerHour rate = new RatePerHour();
        rate.setUser(user);
        rate.setTimeSlot(savedSlot);
        rate.setPrice(request.getHourlyRate());

        ratePerHourRepository.save(rate);

        Map<String, Object> responseData = Map.of(
                "slotId", savedSlot.getId(),
                "rate", rate.getPrice()
        );

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Slot added successfully.",
                responseData
        );
    }

    @Transactional
    public void toggleAvailability(AvailabilityRequest request, HttpServletRequest httpRequest) {
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<TimeSlot> slots = timeSlotRepository.findByUserAndDate(user, request.getDate());
        for (TimeSlot slot : slots) {
            slot.setAvailableToday(request.isAvailable());
        }

        timeSlotRepository.saveAll(slots);
    }
}

