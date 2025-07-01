package com.friday.ai_assistant.service;

import org.springframework.stereotype.Service;

@Service
public class FridayService {

    private final GeminiService geminiService;
    private final InternetService internetService;

    public FridayService(GeminiService geminiService, InternetService internetService) {
        this.geminiService = geminiService;
        this.internetService = internetService;
    }

    public String getResponse(String query) {
        if (!internetService.isInternetAvailable()) {
            return "I'm offline right now. Try again when connected to the internet.";
        }
        return geminiService.askGemini(query);
    }
}