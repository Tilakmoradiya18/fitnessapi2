package com.fitness.fitnessapi.repository;

import com.fitness.fitnessapi.entity.TransactionHistory;
import com.fitness.fitnessapi.entity.User;
import com.fitness.fitnessapi.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    List<TransactionHistory> findByUser(User user);
    List<TransactionHistory> findAllByTypeAndCompletedFalseAndCreatedAtBefore(TransactionType type, LocalDateTime time);
    List<TransactionHistory> findAllByTypeAndCompletedFalseAndConfirmedFalseAndCreatedAtBefore(
            TransactionType type, LocalDateTime beforeTime
    );
    Optional<TransactionHistory> findByRequestIdAndTypeAndOtp(Long requestId, TransactionType type, String otp);
    Optional<TransactionHistory> findByRequestIdAndType(Long requestId, TransactionType type);



}