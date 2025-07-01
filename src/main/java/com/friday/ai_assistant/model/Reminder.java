package com.friday.ai_assistant.model;

public class Reminder {
    private String message;
    private String time; // Format: HH:mm
    private boolean daily;

    public Reminder() {} // Required for Jackson

    public Reminder(String message, String time, boolean daily) {
        this.message = message;
        this.time = time;
        this.daily = daily;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public boolean isDaily() {
        return daily;
    }
}
