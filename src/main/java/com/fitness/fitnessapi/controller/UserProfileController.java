package com.fitness.fitnessapi.controller;

import com.fitness.fitnessapi.dto.ApiSuccessResponse;
import com.fitness.fitnessapi.dto.UserProfileRequest;
import com.fitness.fitnessapi.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService profileService;

    @PostMapping("/setup")
    public ApiSuccessResponse setupProfile(@RequestBody UserProfileRequest request, HttpServletRequest httpRequest) {
        return profileService.setupProfile(request, httpRequest);
    }
}
