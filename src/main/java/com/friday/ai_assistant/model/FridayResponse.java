package com.friday.ai_assistant.model;

public class FridayResponse {
    private String content;

    public FridayResponse(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}