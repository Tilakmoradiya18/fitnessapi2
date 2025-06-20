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
import com.fitness.fitnessapi.dto.TimeSlotResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        // 1. Validate date/time
        LocalDate slotDate = request.getDate();
        LocalTime startTime = request.getStartTime();
        LocalDate today = LocalDate.now();

        if (slotDate.isBefore(today)) {
            throw new IllegalArgumentException("Cannot select a past date.");
        }

        if (slotDate.isEqual(today) && startTime.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("Cannot select a past time today.");
        }

        // 2. Get logged-in user
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 3. Determine isAvailableToday
        boolean isAvailableToday = false;
        if (slotDate.isEqual(today)) {
            List<TimeSlot> existingToday = timeSlotRepository.findByUserAndDateAndActiveTrue(user, slotDate);
            isAvailableToday = existingToday.stream().anyMatch(TimeSlot::isAvailableToday);
        }

        // 4. Save new time slot
        TimeSlot slot = new TimeSlot();
        slot.setUser(user);
        slot.setDate(slotDate);
        slot.setStartTime(startTime);
        slot.setEndTime(request.getEndTime());
        slot.setAvailableToday(isAvailableToday);
        slot.setExpired(false);

        TimeSlot savedSlot = timeSlotRepository.save(slot);

        // ✅ 5. Update or insert user's single rate record
        Optional<RatePerHour> existingRate = ratePerHourRepository.findByUser(user);
        RatePerHour rate;
        if (existingRate.isPresent()) {
            rate = existingRate.get();
            rate.setPrice(request.getHourlyRate()); // ✅ update rate
        } else {
            rate = new RatePerHour();
            rate.setUser(user);
            rate.setPrice(request.getHourlyRate());
        }

        ratePerHourRepository.save(rate);

        // ✅ 6. Return clean response
        Map<String, Object> responseData = Map.of(
                "slotId", savedSlot.getId(),
                "hourlyRate", rate.getPrice(),
                "startTime", savedSlot.getStartTime(),
                "endTime", savedSlot.getEndTime()
        );

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Slot and user's hourly rate saved successfully.",
                responseData
        );
    }


    @Transactional
    public ApiSuccessResponse toggleAvailability(AvailabilityRequest request, HttpServletRequest httpRequest) {
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LocalDate requestDate = request.getDate();
        boolean requestedAvailability = request.isAvailable();

        // ❌ Prevent setting isAvailableToday = true for past dates
        if (requestDate.isBefore(LocalDate.now()) && requestedAvailability) {
            throw new IllegalArgumentException("Cannot set availability to true for past dates.");
        }

        List<TimeSlot> slots = timeSlotRepository.findByUserAndDateAndActiveTrue(user, requestDate);

        for (TimeSlot slot : slots) {
            slot.setAvailableToday(requestedAvailability);
        }

        timeSlotRepository.saveAll(slots);

        Map<String, Object> responseData = Map.of(
                "date", requestDate,
                "available", requestedAvailability,
                "updatedSlots", slots.size()
        );

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Availability updated successfully.",
                responseData
        );
    }



    @Transactional
    public ApiSuccessResponse getTodaySlots(HttpServletRequest httpRequest) {
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LocalDate today = LocalDate.now();

        // ✅ Fetch only active (non-deleted) slots for today
        List<TimeSlot> slots = timeSlotRepository.findByUserAndDateAndActiveTrue(user, today);

        RatePerHour rate = ratePerHourRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Rate not found"));

        // Map time slots (excluding hourly rate from individual slots)
        List<Map<String, Object>> slotList = slots.stream().map(slot -> {
            Map<String, Object> map = new HashMap<>();
            map.put("startTime", slot.getStartTime());
            map.put("endTime", slot.getEndTime());
            return map;
        }).toList();

        // Build response data with hourly rate outside the slot list
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("hourlyRate", rate.getPrice());
        responseData.put("slots", slotList);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Today's slots fetched successfully.",
                responseData
        );
    }


    @Transactional
    public ApiSuccessResponse softDeleteSlotById(Long slotId, HttpServletRequest httpRequest) {
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TimeSlot slot = timeSlotRepository.findByIdAndUserAndActiveTrue(slotId, user)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found or already deleted."));

        slot.setActive(false); // Soft delete
        timeSlotRepository.save(slot);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Slot deleted successfully.",
                Map.of("deletedSlotId", slotId)
        );
    }

}

