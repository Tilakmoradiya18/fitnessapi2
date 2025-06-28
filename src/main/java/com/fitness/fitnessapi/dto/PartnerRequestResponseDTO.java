package com.fitness.fitnessapi.dto;

public class PartnerRequestResponseDTO {
    private Long senderId;
    private String senderName;
    private SlotInfoDTO slot;
    private String status;

    public PartnerRequestResponseDTO(Long senderId, String senderName, SlotInfoDTO slot, String status) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.slot = slot;
        this.status = status;
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
}



