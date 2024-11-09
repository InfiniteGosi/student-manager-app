package com.example.studentmanagement.models;

import java.util.Date;

public class LoginEntry {
    private String timestamp;

    // Empty constructor required for Firebase deserialization
    public LoginEntry() {}

    public LoginEntry(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

