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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

//        profile.setImage(request.getImage());
        // ✅ Handle image file upload
        MultipartFile imageFile = request.getImage();
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Save to local filesystem (for demo), later move to S3 or DB
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                String uploadDir = "uploads/"; // Create this folder if not exists
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Save path or URL to DB
                profile.setImage(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store image: " + e.getMessage());
            }
        }

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

//        profile.setImage(request.getImage());
        // ✅ Handle image file upload
        MultipartFile imageFile = request.getImage();
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Save to local filesystem (for demo), later move to S3 or DB
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                String uploadDir = "uploads/"; // Create this folder if not exists
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Save path or URL to DB
                profile.setImage(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store image: " + e.getMessage());
            }
        }

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
