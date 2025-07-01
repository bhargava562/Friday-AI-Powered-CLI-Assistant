package com.friday.ai_assistant.tools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StockTool {

    @Value("${ALPHA_VANTAGE_API_KEY}")
    private String apiKey;

    public String getStock(String symbol) {
        try {
            String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode quote = new ObjectMapper().readTree(response.body()).get("Global Quote");

            return String.format("📈 Stock: %s\nPrice: $%s\nChange: %s%%",
                    quote.get("01. symbol").asText(),
                    quote.get("05. price").asText(),
                    quote.get("10. change percent").asText());
        } catch (Exception e) {
            return "Stock tool failed: " + e.getMessage();
        }
    }
}
