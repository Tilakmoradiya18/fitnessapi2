package com.fitness.fitnessapi.dto;

public class PartnerRequestResponseDTO {
    private Long requestId;
    private Long senderId;
    private String senderName;
    private SlotInfoDTO slot;
    private String status;
    private Double hourlyRate;

    public PartnerRequestResponseDTO(Long requestId,Long senderId, String senderName, SlotInfoDTO slot, String status,Double hourlyRate) {
        this.requestId = requestId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.slot = slot;
        this.status = status;
        this.hourlyRate = hourlyRate;
    }

    // Getters and setters


    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public SlotInfoDTO getSlot() {
        return slot;
    }

    public void setSlot(SlotInfoDTO slot) {
        this.slot = slot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}



