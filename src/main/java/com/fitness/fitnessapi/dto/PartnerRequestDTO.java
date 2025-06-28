package com.fitness.fitnessapi.dto;

import lombok.Data;

@Data
public class PartnerRequestDTO {
    private Long receiverId;
    private String startTime; // Format: "HH:mm"
    private String endTime;   // Format: "HH:mm"

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
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
}
