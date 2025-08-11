//package com.fitness.fitnessapi.service;
//
//import com.fitness.fitnessapi.dto.*;
//import com.fitness.fitnessapi.entity.ResetToken;
//import com.fitness.fitnessapi.entity.User;
//import com.fitness.fitnessapi.repository.ResetTokenRepository;
//import com.fitness.fitnessapi.repository.UserRepository;
//import com.fitness.fitnessapi.util.JwtUtil;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//public class AuthService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private ResetTokenRepository resetTokenRepository;
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    private Map<String, String> otpStore = new ConcurrentHashMap<>();
//
//
//    public ApiSuccessResponse signup(SignupRequest request) {
//        if (!request.getPassword().equals(request.getConfirmPassword())) {
//            throw new IllegalArgumentException("Passwords and ConfirmPassword do not match");
//        }
//
//        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
//            throw new IllegalArgumentException("Email already registered");
//        }
//
//        User user = new User();
//        user.setName(request.getName());
//        user.setEmail(request.getEmail());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setActive(true);
//
//        userRepository.save(user);
//
//        return new ApiSuccessResponse(
//                LocalDateTime.now(),
//                201,
//                "Signup successful",
//                null
//        );
//    }
//
//
//    public ApiSuccessResponse login(LoginRequest loginRequest) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        loginRequest.getEmail(),
//                        loginRequest.getPassword()
//                )
//        );
//
//        User user = userRepository.findByEmail(loginRequest.getEmail())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid Email or Password"));
//
//        String token = jwtUtil.generateToken(user.getEmail());
//
//        return new ApiSuccessResponse(
//                LocalDateTime.now(),
//                200,
//                "Login successful",
//                new LoginResponse(token, "Login successful")
//        );
//    }
//
//
//    @Transactional
//    public ApiSuccessResponse forgotPassword(ForgotPasswordRequest request) {
//        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
//
//        if (userOpt.isEmpty()) {
//            throw new IllegalArgumentException("Email not registered");
//        }
//
//        if (!"0000".equals(request.getOtp())) {
//            throw new IllegalArgumentException("Invalid OTP");
//        }
//
//        // Generate token and save
//        resetTokenRepository.deleteByEmail(request.getEmail());  // clear old tokens if any
//
//        String token = UUID.randomUUID().toString();
//        ResetToken resetToken = new ResetToken(request.getEmail(), token, LocalDateTime.now().plusMinutes(15));
//        resetTokenRepository.save(resetToken);
//
//        return new ApiSuccessResponse(
//                LocalDateTime.now(),
//                200,
//                "OTP Verified Successfully! Use this token to reset your password.",
//                token  // <-- pass token here so frontend can use it
//        );
//    }
//
//
//    public ApiSuccessResponse resetPassword(ResetPasswordRequest request, String authHeader) {
//        // Extract token from Authorization header
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            throw new IllegalArgumentException("Missing or invalid Authorization header");
//        }
//
//        String token = authHeader.substring(7); // Remove "Bearer "
//
//        ResetToken tokenEntity = resetTokenRepository.findByToken(token)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));
//
//        if (tokenEntity.getExpiry().isBefore(LocalDateTime.now())) {
//            throw new IllegalArgumentException("Token expired");
//        }
//
//        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
//            throw new IllegalArgumentException("Passwords do not match");
//        }
//
//        User user = userRepository.findByEmail(tokenEntity.getEmail())
//                .orElseThrow(() -> new IllegalArgumentException("User not found"));
//
//        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        userRepository.save(user);
//
//        resetTokenRepository.delete(tokenEntity); // Invalidate token
//
//        return new ApiSuccessResponse(
//                LocalDateTime.now(),
//                200,
//                "Password reset successful",
//                null
//        );
//    }
//
//
//    public void logout(HttpServletRequest request) {
//        // Nothing needed here for now
//        // If refresh token/blacklist is added later, you can process it here
//    }
//
//
//    public Map<String, String> getOtpStore() {
//        return otpStore;
//    }
//
//    public void setOtpStore(Map<String, String> otpStore) {
//        this.otpStore = otpStore;
//    }
//}


package com.fitness.fitnessapi.service;

import com.fitness.fitnessapi.dto.*;
import com.fitness.fitnessapi.entity.EmailOtp;
import com.fitness.fitnessapi.entity.ResetToken;
import com.fitness.fitnessapi.entity.User;
import com.fitness.fitnessapi.repository.EmailOtpRepository;
import com.fitness.fitnessapi.repository.ResetTokenRepository;
import com.fitness.fitnessapi.repository.UserRepository;
import com.fitness.fitnessapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ResetTokenRepository resetTokenRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailOtpRepository emailOtpRepository;

    private Map<String, String> otpStore = new ConcurrentHashMap<>(); // left for compatibility if needed

    // ---------------------------
    // Signup - now requires OTP verification
    // ---------------------------
    public ApiSuccessResponse signup(SignupRequest request) {
        if (request.getPassword() == null || request.getConfirmPassword() == null) {
            throw new IllegalArgumentException("Passwords cannot be null");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords and ConfirmPassword do not match");
        }

        String email = request.getEmail();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Check OTP verification
        Optional<EmailOtp> otpOpt = emailOtpRepository.findByEmail(email);
        if (otpOpt.isEmpty() || !otpOpt.get().isVerified()) {
            throw new IllegalArgumentException("OTP verification required before signup");
        }

        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        userRepository.save(user);

        // Cleanup OTP entry after successful signup
//        emailOtpRepository.deleteByEmail(email);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                201,
                "Signup successful",
                null
        );
    }

    // ---------------------------
    // Login (unchanged)
    // ---------------------------
    public ApiSuccessResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Email or Password"));

        String token = jwtUtil.generateToken(user.getEmail());

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Login successful",
                new LoginResponse(token, "Login successful")
        );
    }

    // ---------------------------
    // Forgot password (unchanged)
    // ---------------------------
    @Transactional
    public ApiSuccessResponse forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Email not registered");
        }

        if (!"0000".equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        // Generate token and save
        resetTokenRepository.deleteByEmail(request.getEmail());  // clear old tokens if any

        String token = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken(request.getEmail(), token, LocalDateTime.now().plusMinutes(15));
        resetTokenRepository.save(resetToken);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "OTP Verified Successfully! Use this token to reset your password.",
                token  // <-- pass token here so frontend can use it
        );
    }

    // ---------------------------
    // Reset password (unchanged)
    // ---------------------------
    public ApiSuccessResponse resetPassword(ResetPasswordRequest request, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        ResetToken tokenEntity = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (tokenEntity.getExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = userRepository.findByEmail(tokenEntity.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetTokenRepository.delete(tokenEntity); // Invalidate token

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Password reset successful",
                null
        );
    }

    public void logout(HttpServletRequest request) {
        // Nothing needed here for now
        // If refresh token/blacklist is added later, you can process it here
    }

    // ---------------------------
    // NEW: Request OTP (Step 1)
    // ---------------------------
    public ApiSuccessResponse requestOtp(RequestOtpRequest request) {
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Remove any existing OTP rows for this email
        emailOtpRepository.deleteByEmail(email);

        // generate 4-digit OTP
        String otp = String.format("%04d", new Random().nextInt(10000));

        EmailOtp emailOtp = new EmailOtp(email, otp, false);
        EmailOtp saved = emailOtpRepository.save(emailOtp);

        // Return id and otp (for testing). In production, do NOT return OTP and send it via email/SMS instead.
        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "OTP generated successfully",
                Map.of("id", saved.getId(), "otp", saved.getOtp())
        );
    }

    // ---------------------------
    // NEW: Get OTP by id (Step 2 - testing)
    // ---------------------------
    public ApiSuccessResponse getOtp(Long id) {
        EmailOtp emailOtp = emailOtpRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("OTP record not found"));

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "OTP fetched successfully",
                Map.of(
                        "id", emailOtp.getId(),
                        "email", emailOtp.getEmail(),
                        "otp", emailOtp.getOtp(),
                        "isVerified", emailOtp.isVerified()
                )
        );
    }

    // ---------------------------
    // NEW: Verify OTP (Step 3)
    // ---------------------------
    public ApiSuccessResponse verifyOtp(VerifyOtpRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("OTP id is required");
        }
        if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
            throw new IllegalArgumentException("OTP is required");
        }

        EmailOtp emailOtp = emailOtpRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("OTP record not found"));

        if (emailOtp.isVerified()) {
            throw new IllegalArgumentException("OTP already verified");
        }

        if (!emailOtp.getOtp().equals(request.getOtp().trim())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        emailOtp.setVerified(true);
        emailOtpRepository.save(emailOtp);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "OTP verified successfully",
                Map.of("email", emailOtp.getEmail())
        );
    }

    // getters / setters for otpStore (kept for compatibility)
    public Map<String, String> getOtpStore() {
        return otpStore;
    }

    public void setOtpStore(Map<String, String> otpStore) {
        this.otpStore = otpStore;
    }
}
