package com.fitness.fitnessapi.repository;
import com.fitness.fitnessapi.entity.ForgotPasswordOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgotPasswordOtpRepository extends JpaRepository<ForgotPasswordOtp, Long> {
    void deleteByEmail(String email);
    Optional<ForgotPasswordOtp> findByEmail(String email);

}
