package com.fitness.fitnessapi.controller;

import com.fitness.fitnessapi.dto.ApiSuccessResponse;
import com.fitness.fitnessapi.dto.PartnerRequestDTO;
import com.fitness.fitnessapi.dto.TimeSlotResponseDTO;
import com.fitness.fitnessapi.service.PartnerRequestService;
import com.fitness.fitnessapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/partner-request")
public class PartnerRequestController {

    @Autowired
    private PartnerRequestService requestService;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/send")
    public ResponseEntity<ApiSuccessResponse> sendRequest(
            @RequestBody PartnerRequestDTO dto,
            HttpServletRequest request) {

        Long senderId = jwtUtil.extractUserIdFromRequest(request);
        ApiSuccessResponse response = requestService.sendRequest(senderId, dto);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/available-slots")
    public ResponseEntity<ApiSuccessResponse> getAvailableSlots(@RequestParam("partnerId") Long partnerId) {
        ApiSuccessResponse response = requestService.getAvailableSlotResponse(partnerId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/upcoming")
    public ResponseEntity<ApiSuccessResponse> getUpcomingRequests(HttpServletRequest request) {
        Long receiverId = jwtUtil.extractUserIdFromRequest(request);
        ApiSuccessResponse response = requestService.getUpcomingRequestsForPartner(receiverId);
        return ResponseEntity.ok(response);
    }



}
