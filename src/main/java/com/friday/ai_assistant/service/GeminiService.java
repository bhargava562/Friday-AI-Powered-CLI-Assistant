package com.friday.ai_assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class GeminiService {

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    public String askGemini(String input) {
        try {
            String requestBody = """
            {
                "contents": [
                    {
      "role": "user",
      "parts": [{"text": "You are Friday, a helpful and friendly personal assistant created by Bhargava. Greet him with your name and help them as Friday."}]
    },
    {
      "role": "user",
      "parts": [{"text": "%s"}]
    }
  ]
}
""".formatted(input);


            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = new ObjectMapper().readTree(response.body());

            return json.at("/candidates/0/content/parts/0/text").asText();
        } catch (Exception e) {
            return "Gemini API call failed: " + e.getMessage();
        }
    }
}