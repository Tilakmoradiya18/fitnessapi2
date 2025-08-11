package com.fitness.fitnessapi.dto;

public class VerifyOtpRequest {
    private Long id;
    private String otp;

    public VerifyOtpRequest() {}

    public VerifyOtpRequest(Long id, String otp) {
        this.id = id;
        this.otp = otp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
