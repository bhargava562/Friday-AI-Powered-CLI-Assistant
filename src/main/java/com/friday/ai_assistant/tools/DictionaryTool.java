package com.friday.ai_assistant.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class DictionaryTool {

    public String lookupDefinition(String query) {
        try {
            String word = extractWord(query);
            String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());

            if (!json.isArray() || json.isEmpty()) {
                return "No definition found for \"" + word + "\".";
            }

            JsonNode meanings = json.get(0).get("meanings");
            if (meanings == null || meanings.isEmpty()) return "No meanings available.";

            JsonNode def = meanings.get(0).get("definitions").get(0).get("definition");
            return "Definition of " + word + ": " + def.asText();

        } catch (Exception e) {
            return "Error retrieving dictionary definition: " + e.getMessage();
        }
    }

    private String extractWord(String query) {
        query = query.toLowerCase();
        if (query.startsWith("meaning of ")) {
            return query.substring(11).trim();
        } else if (query.startsWith("define ")) {
            return query.substring(7).trim();
        } else if (query.startsWith("dictionary ")) {
            return query.substring(10).trim();
        }
        return query.trim();
    }
}
