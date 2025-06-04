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

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Profile saved successfully",
                null
        );
    }
}
