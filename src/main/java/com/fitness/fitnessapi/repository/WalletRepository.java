package com.fitness.fitnessapi.repository;

import com.fitness.fitnessapi.entity.User;
import com.fitness.fitnessapi.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser(User user);
}