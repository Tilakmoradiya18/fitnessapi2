package com.fitness.fitnessapi.repository;

import com.fitness.fitnessapi.entity.TimeSlot;
import com.fitness.fitnessapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByUserAndDateAndActiveTrue(User user, LocalDate date);

        Optional<TimeSlot> findByIdAndUserAndActiveTrue(Long id, User user);
        Optional<TimeSlot> findByUserAndDateAndStartTimeAndEndTime(User user, LocalDate date, LocalTime startTime, LocalTime endTime);

}
