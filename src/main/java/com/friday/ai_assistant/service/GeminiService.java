package com.friday.ai_assistant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

            // Check status code
            if (response.statusCode() == 403 || response.statusCode() == 429) {
                return "🚫 Sorry boss, your Gemini API quota is expired or access is denied.";
            }

            JsonNode json = new ObjectMapper().readTree(response.body());

            // Check for empty candidate or content
            JsonNode outputNode = json.at("/candidates/0/content/parts/0/text");
            if (outputNode.isMissingNode() || outputNode.asText().isBlank()) {
                return "⚠️ Gemini did not return a valid response.";
            }

            return outputNode.asText();

        } catch (Exception e) {
            return "❌ Gemini API call failed: " + e.getMessage();
        }
    }
}
