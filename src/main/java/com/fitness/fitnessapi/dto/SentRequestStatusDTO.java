package com.fitness.fitnessapi.dto;

public class SentRequestStatusDTO {
    private Long receiverId;
    private String receiverName;
    private SlotInfoDTO slot;
    private String status;

    public SentRequestStatusDTO(Long receiverId, String receiverName, SlotInfoDTO slot, String status) {
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.slot = slot;
        this.status = status;
    }

    // getters/setters

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
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
