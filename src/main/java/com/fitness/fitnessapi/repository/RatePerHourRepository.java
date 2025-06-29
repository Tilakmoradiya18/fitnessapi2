package com.fitness.fitnessapi.repository;

import com.fitness.fitnessapi.entity.RatePerHour;
import com.fitness.fitnessapi.entity.TimeSlot;
import com.fitness.fitnessapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface RatePerHourRepository extends JpaRepository<RatePerHour, Long> {
    Optional<RatePerHour> findByUser(User user);
    Optional<RatePerHour> findByUserId(Long userId);
}

