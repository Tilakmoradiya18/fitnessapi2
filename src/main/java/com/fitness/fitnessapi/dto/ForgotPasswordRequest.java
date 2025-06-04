// ForgotPasswordRequest.java
package com.fitness.fitnessapi.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
    private String otp;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
