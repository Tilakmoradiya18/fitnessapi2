package com.fitness.fitnessapi.dto;

public class LoginResponse {
    private String token;
    private String message;

    // No-arg constructor
    public LoginResponse() {
    }

    // All-arg constructor
    public LoginResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
