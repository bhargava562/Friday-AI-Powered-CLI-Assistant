package com.friday.ai_assistant.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class QuoteTool {

    public String getRandomQuote() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.quotable.io/random"))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode quote = new ObjectMapper().readTree(response.body());

            return "\"" + quote.get("content").asText() + "\" – " + quote.get("author").asText();
        } catch (Exception e) {
            return "Unable to fetch a quote right now.";
        }
    }
}
