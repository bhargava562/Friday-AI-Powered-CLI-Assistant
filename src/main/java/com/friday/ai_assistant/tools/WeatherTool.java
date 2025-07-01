package com.friday.ai_assistant.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class WeatherTool {

    @Value("${OPENWEATHER_API_KEY}")
    private String apiKey;

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    public String getWeather(String city) {
        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = String.format(BASE_URL, encodedCity, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());

            if (json.has("main") && json.has("weather")) {
                double temp = json.get("main").get("temp").asDouble();
                String description = json.get("weather").get(0).get("description").asText();

                return String.format("🌤️ Weather in %s:\nTemp: %.1f°C\nCondition: %s", city, temp, description);
            } else if (json.has("message")) {
                return "Weather API error: " + json.get("message").asText();
            } else {
                return "Unexpected weather response. Please try again.";
            }

        } catch (Exception e) {
            return "Weather tool failed: " + e.getMessage();
        }
    }
}
