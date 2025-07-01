package com.friday.ai_assistant.tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NewsTool {

    @Value("${GNEWS_API_KEY}")
    private String apiKey;

    public String getTopHeadlines() {
        try {
            String url = "https://gnews.io/api/v4/top-headlines?lang=en&country=in&max=5&token=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode articles = new ObjectMapper().readTree(response.body()).get("articles");

            StringBuilder sb = new StringBuilder("📰 Top News:\n");
            for (JsonNode article : articles) {
                sb.append("• ").append(article.get("title").asText()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Failed to fetch news: " + e.getMessage();
        }
    }
}
