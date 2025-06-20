// TimeSlotResponse.java
package com.fitness.fitnessapi.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeSlotResponse {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private double hourlyRate;

    public TimeSlotResponse(LocalDate date, LocalTime startTime, LocalTime endTime, Double price) {
    }


    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}
