package com.fitness.fitnessapi.controller;

import com.fitness.fitnessapi.dto.ApiSuccessResponse;
import com.fitness.fitnessapi.dto.TimeSlotRequest;
import com.fitness.fitnessapi.dto.AvailabilityRequest;
import com.fitness.fitnessapi.service.AvailabilityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/toggle")
    public ResponseEntity<String> toggleAvailability(@RequestBody AvailabilityRequest request,
                                                     HttpServletRequest httpRequest) {
        availabilityService.toggleAvailability(request, httpRequest);
        return ResponseEntity.ok("Availability updated successfully.");
    }
}
