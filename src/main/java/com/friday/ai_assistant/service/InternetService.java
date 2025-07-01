package com.friday.ai_assistant.service;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Service;

@Service
public class InternetService {

    public boolean isInternetAvailable() {
        try {
            Request.get("https://www.google.com").connectTimeout(Timeout.ofMilliseconds(1000)).execute().returnContent();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}