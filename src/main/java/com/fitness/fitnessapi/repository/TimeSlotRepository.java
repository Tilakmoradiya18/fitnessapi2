package com.fitness.fitnessapi.repository;

import com.fitness.fitnessapi.entity.TimeSlot;
import com.fitness.fitnessapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
        List<TimeSlot> findByUserAndIsDeletedFalse(User user);
        List<TimeSlot> findByUserAndDateAndIsDeletedFalse(User user,LocalDate date);
        List<TimeSlot> findByUser(User user);
        Optional<TimeSlot> findByUserAndDateAndStartTimeAndEndTime(User user, LocalDate date, LocalTime startTime, LocalTime endTime);

}
