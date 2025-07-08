package com.fitness.fitnessapi.repository;

import com.fitness.fitnessapi.entity.TransactionHistory;
import com.fitness.fitnessapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    List<TransactionHistory> findByUser(User user);
}