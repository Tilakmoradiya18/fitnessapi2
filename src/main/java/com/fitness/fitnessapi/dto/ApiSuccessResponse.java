package com.fitness.fitnessapi.dto;

import java.time.LocalDateTime;

public class ApiSuccessResponse {
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private Object data;

    public ApiSuccessResponse(LocalDateTime timestamp, int status, String message, Object data) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
