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

import com.fitness.fitnessapi.dto.*;
import com.fitness.fitnessapi.entity.*;
import com.fitness.fitnessapi.repository.*;
import com.fitness.fitnessapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
//import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        LocalDate slotDate = request.getDate();
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();
        LocalDate today = LocalDate.now();

        // ✅ Get logged-in user
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ✅ CLEAR ALL condition: if startTime or endTime is null => Soft delete all
        if (startTime == null || endTime == null) {
            List<TimeSlot> allSlots = timeSlotRepository.findByUser(user);
            for (TimeSlot slot : allSlots) {
                slot.setDeleted(true);
            }
            timeSlotRepository.saveAll(allSlots);

            return new ApiSuccessResponse(
                    LocalDateTime.now(),
                    200,
                    "All previous slots cleared successfully.",
                    Map.of("clearedSlots", allSlots.size())
            );
        }

        // ✅ Validation if date/time are valid
        if (slotDate.isBefore(today)) {
            throw new IllegalArgumentException("Cannot select a past date.");
        }

        if (slotDate.isEqual(today) && startTime.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("Cannot select a past time today.");
        }

        // ✅ 1. Check for existing slot
        Optional<TimeSlot> existingSlot = timeSlotRepository
                .findByUserAndDateAndStartTimeAndEndTime(user, slotDate, startTime, endTime);

        // ✅ 2. Always update rate regardless of slot duplication
        Optional<RatePerHour> existingRate = ratePerHourRepository.findByUser(user);
        RatePerHour rate;
        if (existingRate.isPresent()) {
            rate = existingRate.get();
            rate.setPrice(request.getHourlyRate()); // ✅ update to new rate always
        } else {
            rate = new RatePerHour();
            rate.setUser(user);
            rate.setPrice(request.getHourlyRate());
        }
        ratePerHourRepository.save(rate);

        if (existingSlot.isPresent()) {
            // Slot already exists, don’t insert again
            Map<String, Object> responseData = Map.of(
                    "slotId", existingSlot.get().getId(),
                    "hourlyRate", rate.getPrice(),
                    "startTime", existingSlot.get().getStartTime(),
                    "endTime", existingSlot.get().getEndTime(),
                    "slotExists", true
            );

            return new ApiSuccessResponse(
                    LocalDateTime.now(),
                    200,
                    "Slot already exists, rate updated successfully.",
                    responseData
            );
        }

        // ✅ 3. Determine isAvailableToday
        boolean isAvailableToday = false;
        if (slotDate.isEqual(today)) {
            List<TimeSlot> existingToday = timeSlotRepository.findByUserAndDateAndIsDeletedFalse(user, slotDate);
            isAvailableToday = existingToday.stream().anyMatch(TimeSlot::isAvailableToday);
        }

        // ✅ 4. Create new slot if it doesn’t exist
        TimeSlot slot = new TimeSlot();
        slot.setUser(user);
        slot.setDate(slotDate);
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setAvailableToday(isAvailableToday);
        slot.setExpired(false);
        slot.setActive(true);
        slot.setDeleted(false); // very important

        TimeSlot savedSlot = timeSlotRepository.save(slot);

        Map<String, Object> responseData = Map.of(
                "slotId", savedSlot.getId(),
                "hourlyRate", rate.getPrice(),
                "startTime", savedSlot.getStartTime(),
                "endTime", savedSlot.getEndTime(),
                "slotExists", false
        );

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Slot and rate saved successfully.",
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

        List<TimeSlot> slots = timeSlotRepository.findByUserAndDateAndIsDeletedFalse(user, requestDate);

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
    public ApiSuccessResponse getUniqueTimeSlots(HttpServletRequest httpRequest) {
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1. Fetch all active slots (across all dates)
        List<TimeSlot> slots = timeSlotRepository.findByUserAndIsDeletedFalse(user);

        // 2. Extract unique (startTime, endTime) combinations
        Set<String> seen = new HashSet<>();
        List<Map<String, Object>> uniqueSlotList = new ArrayList<>();

        for (TimeSlot slot : slots) {
            String key = slot.getStartTime() + "-" + slot.getEndTime();
            if (seen.add(key)) {
                uniqueSlotList.add(Map.of(
                        "startTime", slot.getStartTime().toString(),
                        "endTime", slot.getEndTime().toString()
                ));
            }
        }

        // 3. Fetch user's hourly rate
        RatePerHour rate = ratePerHourRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Rate not found"));

        // 4. Response structure
        Map<String, Object> responseData = Map.of(
                "slots", uniqueSlotList,
                "hourlyRate", rate.getPrice()
        );

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "slots fetched successfully.",
                responseData
        );
    }


    public ApiSuccessResponse softDeleteSlotById(Long slotId, HttpServletRequest request) {
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(request));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        if (!slot.getUser().equals(user)) {
            throw new AccessDeniedException("Unauthorized slot access.");
        }

        slot.setDeleted(true);
        timeSlotRepository.save(slot);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Slot soft deleted successfully.",
                Map.of("slotId", slotId)
        );
    }

    public ApiSuccessResponse getAvailablePartners(int page, int size) {
        LocalDate today = LocalDate.now();
        Pageable pageable = PageRequest.of(page, size);

        Page<User> userPage = timeSlotRepository.findAvailableUsersForToday(today, pageable); // no repo rename

        List<Map<String, Object>> partners = userPage.getContent().stream().map(user -> {
            UserProfile profile = user.getUserProfile();

            // ✅ Fetch only today's active and non-expired slots
            List<TimeSlot> validSlots = timeSlotRepository.findByUserAndDateAndIsDeletedFalse(user, today)
                    .stream()
                    .filter(slot -> !slot.isExpired())
                    .toList();

            // ✅ Map slots (startTime & endTime)
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            List<Map<String, String>> slots = validSlots.stream().map(slot -> {
                Map<String, String> slotMap = new LinkedHashMap<>();
                slotMap.put("startTime", slot.getStartTime().format(timeFormatter));
                slotMap.put("endTime", slot.getEndTime().format(timeFormatter));
                return slotMap;
            }).toList();


            // ✅ Construct partner response map
            Map<String, Object> partner = new LinkedHashMap<>();
            partner.put("userId", user.getId());
            partner.put("fullName", profile.getFullName());
            partner.put("email", user.getEmail());
            partner.put("image", profile.getImage());
            partner.put("bio", profile.getBio());
            partner.put("gender", profile.getGender());
            partner.put("dateOfBirth", profile.getDateOfBirth());
            partner.put("city", profile.getCity());
            partner.put("zipCode", profile.getZipCode());
            partner.put("country", profile.getCountry());
            partner.put("slots", slots); // ✅ valid slots

            return partner;
        }).toList();

        // ✅ Final response
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("totalElements", userPage.getTotalElements());
        responseData.put("totalPages", userPage.getTotalPages());
        responseData.put("currentPage", userPage.getNumber());
        responseData.put("partners", partners);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Paginated available partners with slots fetched.",
                responseData
        );
    }


}

