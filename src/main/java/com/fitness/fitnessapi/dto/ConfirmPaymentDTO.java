package com.fitness.fitnessapi.dto;

public class ConfirmPaymentDTO {
    private Long transactionId;
    private String otp;

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    // Getters and Setters
}
