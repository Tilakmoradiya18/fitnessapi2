package com.fitness.fitnessapi.controller;

import com.fitness.fitnessapi.dto.ApiSuccessResponse;
import com.fitness.fitnessapi.dto.TimeSlotRequest;
import com.fitness.fitnessapi.dto.AvailabilityRequest;
import com.fitness.fitnessapi.service.AvailabilityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    @Autowired
    private  AvailabilityService availabilityService;

    @PostMapping("/add-slot")
    public ResponseEntity<ApiSuccessResponse> addSlot(
            @RequestBody TimeSlotRequest request,
            HttpServletRequest httpRequest
    ) {
        ApiSuccessResponse response = availabilityService.addTimeSlot(request, httpRequest);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/toggle")
    public ResponseEntity<ApiSuccessResponse> toggleAvailability(
            @RequestBody AvailabilityRequest request,
            HttpServletRequest httpRequest
    ) {
        ApiSuccessResponse response = availabilityService.toggleAvailability(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    public ResponseEntity<ApiSuccessResponse> getTodaySlots(HttpServletRequest request) {
        ApiSuccessResponse response = availabilityService.getTodaySlots(request);
        return ResponseEntity.ok(response);
    }


//    @DeleteMapping("/delete")
//    public ResponseEntity<ApiSuccessResponse> softDeleteSlots(
//            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
//            HttpServletRequest request) {
//
//        ApiSuccessResponse response = availabilityService.softDeleteSlots(date, request);
//        return ResponseEntity.ok(response);
//    }

    @DeleteMapping("/delete/{slotId}")
    public ResponseEntity<ApiSuccessResponse> softDeleteSlotById(
            @PathVariable("slotId") Long slotId,
            HttpServletRequest request) {

        ApiSuccessResponse response = availabilityService.softDeleteSlotById(slotId, request);
        return ResponseEntity.ok(response);
    }



}
