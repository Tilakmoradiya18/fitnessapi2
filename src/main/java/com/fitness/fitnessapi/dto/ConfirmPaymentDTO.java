package com.fitness.fitnessapi.dto;

public class ConfirmPaymentDTO {
    private Long requestId;
    private String otp;

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    // Getters and Setters
}
