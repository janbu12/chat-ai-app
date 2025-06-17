package com.example.aichatapi.models;

public class ChatMessage {
    public static final int SENDER_AI = 0;
    public static final int SENDER_USER = 1;

    private String content;
    private int sender; // 0 for AI, 1 for User

    public ChatMessage(String content, int sender) {
        this.content = content;
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public int getSender() {
        return sender;
    }
}
