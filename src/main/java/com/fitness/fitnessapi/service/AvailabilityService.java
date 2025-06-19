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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        // Validate date/time
        LocalDate slotDate = request.getDate();
        LocalTime startTime = request.getStartTime();
        LocalDate today = LocalDate.now();

        if (slotDate.isBefore(today)) {
            throw new IllegalArgumentException("Cannot select a past date.");
        }

        if (slotDate.isEqual(today) && startTime.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("Cannot select a past time today.");
        }

        // Extract user from token
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ✅ Check if today's slots already exist and any are marked available
        boolean isAvailableToday = false;
        if (slotDate.isEqual(today)) {
            List<TimeSlot> existingToday = timeSlotRepository.findByUserAndDate(user, slotDate);
            isAvailableToday = existingToday.stream().anyMatch(TimeSlot::isAvailableToday);
        }

        // ✅ Create and save time slot
        TimeSlot slot = new TimeSlot();
        slot.setUser(user);
        slot.setDate(slotDate);
        slot.setStartTime(startTime);
        slot.setEndTime(request.getEndTime());
        slot.setAvailableToday(isAvailableToday);
        slot.setExpired(false);

        TimeSlot savedSlot = timeSlotRepository.save(slot);

        // ✅ Create and save rate
        RatePerHour rate = new RatePerHour();
        rate.setUser(user);
        rate.setTimeSlot(savedSlot);
        rate.setPrice(request.getHourlyRate());

        ratePerHourRepository.save(rate);

        // ✅ Build and return response
        Map<String, Object> responseData = Map.of(
                "slotId", savedSlot.getId(),
                "isAvailableToday", savedSlot.isAvailableToday(),
                "rate/hour($)", rate.getPrice()
        );

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Slot added successfully.",
                responseData
        );
    }


    @Transactional
    public ApiSuccessResponse toggleAvailability(AvailabilityRequest request, HttpServletRequest httpRequest) {
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<TimeSlot> slots = timeSlotRepository.findByUserAndDate(user, request.getDate());

        for (TimeSlot slot : slots) {
            slot.setAvailableToday(request.isAvailable());
        }

        timeSlotRepository.saveAll(slots);

        Map<String, Object> responseData = Map.of(
                "date", request.getDate(),
                "available", request.isAvailable(),
                "updatedSlots", slots.size()
        );

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Availability updated successfully.",
                responseData
        );
    }

}

