package com.friday.ai_assistant.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class JokeTool {

    public String getRandomJoke() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://official-joke-api.appspot.com/random_joke"))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode joke = new ObjectMapper().readTree(response.body());

            return joke.get("setup").asText() + " " + joke.get("punchline").asText();
        } catch (Exception e) {
            return "Sorry, I couldn't fetch a joke right now.";
        }
    }
}
