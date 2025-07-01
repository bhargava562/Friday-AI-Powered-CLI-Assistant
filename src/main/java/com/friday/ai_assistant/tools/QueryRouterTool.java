package com.friday.ai_assistant.tools;

import com.friday.ai_assistant.service.GeminiService;
import org.springframework.stereotype.Component;

@Component
public class QueryRouterTool {

    private final GeminiService geminiService;

    public QueryRouterTool(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public String classify(String userQuery) {
        // 🌐 Step 1: Ask Gemini
        String prompt = """
                Classify the following user query into one of these categories:
                WEATHER, TIME, NEWS, JOKE, STOCK, KNOWLEDGE, QUOTE, DICTIONARY, REMINDER, CURRENCY

                Just reply with the category name. Do not explain anything.

                Query: "%s"
                Category:
                """.formatted(userQuery);

        String response = geminiService.askGemini(prompt).trim().toUpperCase();

        // 🧠 Step 2: Accept if valid
        for (String valid : VALID_CATEGORIES) {
            if (response.contains(valid)) {
                return valid;
            }
        }

        // 🔁 Step 3: NLP fallback if Gemini fails
        String fallback = keywordFallback(userQuery);
        return fallback;
    }

    private String keywordFallback(String query) {
        query = query.toLowerCase();

        if (query.matches(".*\\b(weather|climate|temperature)\\b.*")) return "WEATHER";
        if (query.matches(".*\\b(time|clock|current time)\\b.*")) return "TIME";
        if (query.matches(".*\\b(news|headlines|latest news)\\b.*")) return "NEWS";
        if (query.matches(".*\\b(joke|laugh|funny)\\b.*")) return "JOKE";
        if (query.matches(".*\\b(stock|price of|share)\\b.*")) return "STOCK";
        if (query.matches(".*\\b(quote|motivation|inspiration)\\b.*")) return "QUOTE";
        if (query.matches(".*\\b(dictionary|define|meaning of)\\b.*")) return "DICTIONARY";
        if (query.matches(".*\\b(remind|reminder|alert|reminders|tasks|task)\\b.*")) return "REMINDER";
        if (query.matches(".*\\b(convert|rupee|dollar|euro|inr|usd|currency)\\b.*")) return "CURRENCY";
        if (query.matches(".*\\b(who|what|when|where|why|how|explain)\\b.*")) return "KNOWLEDGE";

        return "UNKNOWN";
    }

    private static final String[] VALID_CATEGORIES = {
            "WEATHER", "TIME", "NEWS", "JOKE", "STOCK",
            "KNOWLEDGE", "QUOTE", "DICTIONARY", "REMINDER", "CURRENCY"
    };
}
