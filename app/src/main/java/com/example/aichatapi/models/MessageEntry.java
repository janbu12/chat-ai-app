package com.example.aichatapi.models;

import com.google.gson.annotations.SerializedName;

public class MessageEntry {
    @SerializedName("type")
    private String type; // "human" atau "ai"
    @SerializedName("data")
    private MessageData data;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MessageData getData() {
        return data;
    }

    public void setData(MessageData data) {
        this.data = data;
    }
}