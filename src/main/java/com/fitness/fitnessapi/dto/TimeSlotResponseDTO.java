package com.fitness.fitnessapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

public class TimeSlotResponseDTO {
    private Long slotId;
    private String startTime;
    private String endTime;

    public TimeSlotResponseDTO(Long slotId,String startTime, String endTime) {
        this.slotId = slotId;
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }
}
