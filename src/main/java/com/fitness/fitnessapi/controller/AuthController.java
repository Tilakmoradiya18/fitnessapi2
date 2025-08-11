package com.fitness.fitnessapi.controller;

import com.fitness.fitnessapi.dto.*;
import com.fitness.fitnessapi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ApiSuccessResponse signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }


    @PostMapping("/login")
    public ApiSuccessResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ApiSuccessResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<ApiSuccessResponse> resetPassword(
            @RequestBody ResetPasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.resetPassword(request, authHeader));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Logged out successfully.");
    }

    /**
     * Step 1: request otp (body: { "email": "..." } )
     * Response data contains id and otp (for testing; remove otp in production)
     */
    @PostMapping("/request-otp")
    public ResponseEntity<ApiSuccessResponse> requestOtp(@RequestBody RequestOtpRequest request) {
        return ResponseEntity.ok(authService.requestOtp(request));
    }

    /**
     * Step 2: get otp by id (testing only)
     * GET /api/auth/get-otp?id={id}
     */
    @GetMapping("/get-otp")
    public ResponseEntity<ApiSuccessResponse> getOtp(@RequestParam("id") Long id) {
        return ResponseEntity.ok(authService.getOtp(id));
    }

    /**
     * Step 3: verify otp (body: { "id": 1, "otp": "1234" })
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiSuccessResponse> verifyOtp(@RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }
}
