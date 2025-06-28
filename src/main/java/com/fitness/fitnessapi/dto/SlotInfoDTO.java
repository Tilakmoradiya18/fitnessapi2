package com.fitness.fitnessapi.dto;

public class SlotInfoDTO {
    private Long slotId;
    private String startTime;
    private String endTime;
    private String date;

    public SlotInfoDTO(Long slotId, String startTime, String endTime, String date) {
        this.slotId = slotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
    }

    // Getters and setters

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
