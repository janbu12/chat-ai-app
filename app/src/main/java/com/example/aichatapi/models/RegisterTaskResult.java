package com.example.aichatapi.models;

public class RegisterTaskResult {
    public boolean success;
    public String message;
    public int httpCode;

    public RegisterTaskResult(boolean success, String message, int httpCode) {
        this.success = success;
        this.message = message;
        this.httpCode = httpCode;
    }
}