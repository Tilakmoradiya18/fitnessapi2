package com.fitness.fitnessapi.service;

import com.fitness.fitnessapi.dto.UserProfileRequest;
import com.fitness.fitnessapi.dto.ApiSuccessResponse;
import com.fitness.fitnessapi.entity.User;
import com.fitness.fitnessapi.entity.UserProfile;
import com.fitness.fitnessapi.repository.UserProfileRepository;
import com.fitness.fitnessapi.repository.UserRepository;
import com.fitness.fitnessapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public ApiSuccessResponse setupProfile(UserProfileRequest request, HttpServletRequest httpRequest) {
        String token = jwtUtil.extractToken(httpRequest);
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserProfile profile = profileRepository.findByUser(user).orElse(new UserProfile());

        profile.setUser(user);
        profile.setEmail(user.getEmail()); // ✅ store login user's email

        profile.setImage(request.getImage());
        profile.setFullName(request.getFullName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setBio(request.getBio());
        profile.setCountry(request.getCountry());
        profile.setCity(request.getCity());
        profile.setZipCode(request.getZipCode());

        profileRepository.save(profile);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("fullName", profile.getFullName());
        responseData.put("email", profile.getEmail());

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                201,
                "Profile setup completed successfully.",
                responseData
        );
    }


    public ApiSuccessResponse getProfile(HttpServletRequest httpRequest) {
        String token = jwtUtil.extractToken(httpRequest);
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        // Prepare response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("fullName", profile.getFullName());
        responseData.put("email", profile.getEmail());
        responseData.put("image", profile.getImage());
        responseData.put("dateOfBirth", profile.getDateOfBirth());
        responseData.put("gender", profile.getGender());
        responseData.put("bio", profile.getBio());
        responseData.put("country", profile.getCountry());
        responseData.put("city", profile.getCity());
        responseData.put("zipCode", profile.getZipCode());

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Profile fetched successfully.",
                responseData
        );
    }


    public ApiSuccessResponse updateProfile(UserProfileRequest request, HttpServletRequest httpRequest) {
        String token = jwtUtil.extractToken(httpRequest);
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        profile.setImage(request.getImage());
        profile.setFullName(request.getFullName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setBio(request.getBio());
        profile.setCountry(request.getCountry());
        profile.setCity(request.getCity());
        profile.setZipCode(request.getZipCode());

        profileRepository.save(profile);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("fullName", profile.getFullName());
        responseData.put("email", profile.getEmail());

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Profile updated successfully.",
                responseData
        );
    }


}
