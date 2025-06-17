package com.example.aichatapi.models;

import com.google.gson.annotations.SerializedName;

public class MessageData {
    @SerializedName("content")
    private String content;

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    // Anda bisa menambahkan properti lain seperti additional_kwargs, response_metadata jika diperlukan
}