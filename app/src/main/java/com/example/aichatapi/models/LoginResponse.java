package com.example.aichatapi.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("success")
    private boolean success;
    @SerializedName("message")
    private String message;
    @SerializedName("user")
    private User user; // Mengacu pada kelas User yang sudah dibuat
    @SerializedName("token")
    private String token;

    // Constructor
    public LoginResponse(boolean success, String message, User user, String token, Object o) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.token = token;
    }

    // Getters (Generate otomatis)
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }


    // Setters (Opsional)
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
