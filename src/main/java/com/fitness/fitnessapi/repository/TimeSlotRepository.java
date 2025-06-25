package com.fitness.fitnessapi.repository;

import com.fitness.fitnessapi.entity.TimeSlot;
import com.fitness.fitnessapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
        List<TimeSlot> findByUserAndIsDeletedFalse(User user);
        List<TimeSlot> findByUserAndDateAndIsDeletedFalse(User user,LocalDate date);
        List<TimeSlot> findByUser(User user);
        Optional<TimeSlot> findByUserAndDateAndStartTimeAndEndTime(User user, LocalDate date, LocalTime startTime, LocalTime endTime);

        @Query("SELECT DISTINCT ts.user FROM TimeSlot ts WHERE ts.date = :today AND ts.isAvailableToday = true AND ts.isDeleted = false AND ts.isExpired = false")
        Page<User> findAvailableUsersForToday(@Param("today") LocalDate today, Pageable pageable);

}
