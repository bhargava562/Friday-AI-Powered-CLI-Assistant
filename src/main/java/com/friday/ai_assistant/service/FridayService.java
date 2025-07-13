package com.friday.ai_assistant.service;

import com.friday.ai_assistant.tools.*;
import org.springframework.stereotype.Service;

@Service
public class FridayService {
    private final GeminiService geminiService;
    private final InternetService internetService;
    private final QueryRouterTool queryRouterTool;

    private final WeatherTool weatherTool;
    private final TimeTool timeTool;
    private final NewsTool newsTool;
    private final JokeTool jokeTool;
    private final StockTool stockTool;
    private final KnowledgeTool knowledgeTool;
    private final QuoteTool quoteTool;
    private final DictionaryTool dictionaryTool;
    private final ReminderTool reminderTool;
    private final CurrencyConverterTool currencyTool;
    private final MailTool mailTool;
    private final AchievementTracker achievementTracker;
    private final GoogleCalendarService calendarService;

    public FridayService(
            GeminiService geminiService,
            InternetService internetService,
            QueryRouterTool queryRouterTool,
            WeatherTool weatherTool,
            TimeTool timeTool,
            NewsTool newsTool,
            JokeTool jokeTool,
            StockTool stockTool,
            KnowledgeTool knowledgeTool,
            QuoteTool quoteTool,
            DictionaryTool dictionaryTool,
            ReminderTool reminderTool,
            CurrencyConverterTool currencyTool,
            MailTool mailTool,
            AchievementTracker achievementTracker,
            GoogleCalendarService calendarService
            ) {
        this.geminiService = geminiService;
        this.internetService = internetService;
        this.queryRouterTool = queryRouterTool;
        this.weatherTool = weatherTool;
        this.timeTool = timeTool;
        this.newsTool = newsTool;
        this.jokeTool = jokeTool;
        this.stockTool = stockTool;
        this.knowledgeTool = knowledgeTool;
        this.quoteTool = quoteTool;
        this.dictionaryTool = dictionaryTool;
        this.reminderTool = reminderTool;
        this.currencyTool = currencyTool;
        this.mailTool = mailTool;
        this.achievementTracker =  achievementTracker;
        this.calendarService = calendarService;
    }
    private String extractBetween(String input, String start, String endRegex) {
        return input.replaceAll(".*?" + start, "").replaceAll(endRegex + ".*", "").trim();
    }
    public String handleCalendarQuery(String query) {
        query = query.toLowerCase().trim();

        // --- LIST EVENTS
        if (query.contains("show") || query.contains("list") || query.contains("upcoming")) {
            return calendarService.listEvents();
        }

        // --- DELETE EVENT
        if (query.contains("delete") || query.contains("remove") || query.contains("cancel")) {
            // Extract event title
            String title = query.replaceAll(".*(delete|remove|cancel)\\s+(event)?", "").trim().replace("\"", "");
            return calendarService.deleteEvent(title);
        }

        // --- ADD EVENT
        if (query.contains("add") || query.contains("create") || query.contains("schedule")) {
            // Example: add event "Doctor Appointment" on 25 July 2025 at 5pm
            String summaryRegex = "\"([^\"]+)\"";
            String[] parts = query.split("on|at|after", 2);
            String summary = "Untitled Event";
            String time = "tomorrow";

            if (query.matches(".*\"[^\"]+\".*")) {
                summary = query.replaceAll(".*\"([^\"]+)\".*", "$1");
            }

            if (parts.length > 1) {
                time = parts[1].replaceAll("\"", "").trim();
            }

            return calendarService.addEvent(summary, time);
        }

        return "❓ Unrecognized event command. Try: add, delete, list calendar events.";
    }
    public String getResponse(String query) {
        if (!internetService.isInternetAvailable()) {
            return "I'm offline right now. Try again when connected to the internet.";
        }

        if (query.equalsIgnoreCase("thanos")) return achievementTracker.showGauntlet();
        if (query.equalsIgnoreCase("thanos snap")) return achievementTracker.snap();
        if (query.toLowerCase().startsWith("collect ")) {
            return achievementTracker.collectStone(query.substring(8).trim());
        }

        // ✅ Mail Shortcut
        if (query.toLowerCase().contains("mail to") &&
                query.toLowerCase().contains("subject:") &&
                query.toLowerCase().contains("description:")) {
            return mailTool.send(query);
        }

        // 🧠 Smart Classification
        String category = queryRouterTool.classify(query);
        return switch (category) {
            case "WEATHER" -> {
                String city = extractCity(query);
                yield weatherTool.getWeather(city);
            }
            case "EVENT" -> handleCalendarQuery(query);
            case "TIME" -> timeTool.getCurrentTime();
            case "NEWS" -> newsTool.getTopHeadlines();
            case "JOKE" -> jokeTool.getRandomJoke();
            case "STOCK" -> {
                String symbol = extractStockSymbol(query);
                yield stockTool.getStock(symbol);
            }
            case "KNOWLEDGE" -> knowledgeTool.getAnswer(query);
            case "QUOTE" -> quoteTool.getRandomQuote();
            case "DICTIONARY" -> dictionaryTool.lookupDefinition(query);
            case "REMINDER" -> {
                if (query.contains("show") || query.contains("list")) {
                    yield reminderTool.listReminders();
                } else if (query.contains("delete") || query.contains("cancel")) {
                    yield reminderTool.cancelReminder(query);
                } else {
                    yield reminderTool.setReminder(query);
                }
            }
            case "CURRENCY" -> currencyTool.convertCurrency(query);
            default -> geminiService.askGemini(query);
        };
    }

    // --- Helper: Extract City from weather-related queries
    private String extractCity(String query) {
        return query.toLowerCase()
                .replaceAll("(what's|what is|tell me|show me|current|today|now|weather|climate|in|at|of)", "")
                .replaceAll("\\d{1,2}\\s*(am|pm)", "")
                .replaceAll("\\d{1,2}:\\d{2}\\s*(am|pm)?", "")
                .replaceAll("[^a-zA-Z\\s]", "")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    // --- Helper: Extract Stock Symbol
    private String extractStockSymbol(String query) {
        if (query.toLowerCase().contains("of")) {
            return query.substring(query.toLowerCase().lastIndexOf("of") + 2).trim().toUpperCase();
        } else if (query.toLowerCase().contains("price")) {
            return query.replaceAll(".*price of", "").trim().toUpperCase();
        }
        return "AAPL"; // fallback
    }
}
