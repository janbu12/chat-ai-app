package com.example.aichatapi.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatHistoryResponse {
    @SerializedName("_id")
    private String id;
    @SerializedName("sessionId")
    private String sessionId;
    @SerializedName("messages")
    private List<MessageEntry> messages;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<MessageEntry> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageEntry> messages) {
        this.messages = messages;
    }
}