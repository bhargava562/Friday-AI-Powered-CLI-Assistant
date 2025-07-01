package com.friday.ai_assistant.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CurrencyConverterTool {

    @Value("${ALPHA_VANTAGE_API_KEY}")
    private String apiKey;

    private static final Map<String, String> currencyMap = new HashMap<>();

    static {
        currencyMap.put("dollar", "USD");
        currencyMap.put("usd", "USD");
        currencyMap.put("euro", "EUR");
        currencyMap.put("pound", "GBP");
        currencyMap.put("rupee", "INR");
        currencyMap.put("inr", "INR");
        currencyMap.put("yen", "JPY");
        currencyMap.put("jpy", "JPY");
        currencyMap.put("won", "KRW");
        currencyMap.put("cad", "CAD");
        currencyMap.put("aud", "AUD");
    }

    public String convertCurrency(String query) {
        try {
            // Extract amount and currencies from flexible query
            Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)?\\s*([a-zA-Z]+).*?(to|in)\\s+([a-zA-Z]+)");
            Matcher matcher = pattern.matcher(query);

            if (!matcher.find()) {
                return "Please ask something like 'Convert 10 USD to INR' or 'What is the value of 1 dollar in rupee?'.";
            }

            double amount = matcher.group(1) != null ? Double.parseDouble(matcher.group(1)) : 1.0;
            String fromRaw = matcher.group(3).toLowerCase();
            String toRaw = matcher.group(5).toLowerCase();

            String from = currencyMap.getOrDefault(fromRaw, fromRaw.toUpperCase());
            String to = currencyMap.getOrDefault(toRaw, toRaw.toUpperCase());

            String url = String.format(
                    "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=%s&to_currency=%s&apikey=%s",
                    from, to, apiKey
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = new ObjectMapper().readTree(response.body());

            JsonNode rateNode = json.at("/Realtime Currency Exchange Rate/5. Exchange Rate");

            if (rateNode.isMissingNode()) {
                return "Couldn't fetch exchange rate. Please check currency names or codes.";
            }

            double rate = Double.parseDouble(rateNode.asText());
            double result = rate * amount;

            return String.format("%.2f %s = %.2f %s", amount, from, result, to);

        } catch (Exception e) {
            return "Failed to fetch currency data: " + e.getMessage();
        }
    }
}
