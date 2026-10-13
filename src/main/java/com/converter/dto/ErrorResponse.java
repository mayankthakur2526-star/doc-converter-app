package com.converter.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
    private boolean success;
    private String error;
    private String details;
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.success = false;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String error) {
        this.success = false;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String error, String details) {
        this.success = false;
        this.error = error;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
