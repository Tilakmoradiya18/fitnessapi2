package com.fitness.fitnessapi.repository;

import com.fitness.fitnessapi.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findByEmail(String email);
    void deleteByEmail(String email);
}
