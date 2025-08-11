package com.fitness.fitnessapi.dto;

public class RequestOtpRequest {
    private String email;

    public RequestOtpRequest() {}

    public RequestOtpRequest(String email) { this.email = email; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
