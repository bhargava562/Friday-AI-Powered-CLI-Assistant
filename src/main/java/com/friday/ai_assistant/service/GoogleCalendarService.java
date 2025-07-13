package com.friday.ai_assistant.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.joestelmach.natty.Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleCalendarService {

    @Value("${google.oauth.credentials.path}")
    private String credentialsPath;

    @Value("${google.calendar.application.name}")
    private String applicationName;

    @Value("${google.calendar.tokens.directory}")
    private String tokensDirectory;

    @Value("${google.calendar.notification.email}")
    private boolean emailNotify;

    @Value("${google.calendar.notification.mobile}")
    private boolean popupNotify;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CALENDAR_ID = "primary";

    private Calendar calendar;

    @PostConstruct
    public void init() throws Exception {
        final var transport = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = getClass().getResourceAsStream("/credentials.json");
        if (in == null) throw new RuntimeException("credentials.json not found!");

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport, JSON_FACTORY, clientSecrets, Collections.singletonList("https://www.googleapis.com/auth/calendar"))
                .setDataStoreFactory(new com.google.api.client.util.store.FileDataStoreFactory(new File(tokensDirectory)))
                .setAccessType("offline")
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

        this.calendar = new Calendar.Builder(transport, JSON_FACTORY, credential)
                .setApplicationName(applicationName)
                .build();
    }

    public String addEvent(String summary, String naturalDate) {
        try {
            Date startDate = parseDate(naturalDate);
            if (startDate == null) return "❌ Invalid date or time!";

            Date endDate = new Date(startDate.getTime() + 60 * 60 * 1000); // +1 hour

            List<EventReminder> reminders = new ArrayList<>();
            if (emailNotify) reminders.add(new EventReminder().setMethod("email").setMinutes(10));
            if (popupNotify) reminders.add(new EventReminder().setMethod("popup").setMinutes(5));

            Event event = new Event()
                    .setSummary(summary)
                    .setStart(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(startDate)))
                    .setEnd(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(endDate)))
                    .setReminders(new Event.Reminders()
                            .setUseDefault(false)
                            .setOverrides(reminders));

            calendar.events().insert(CALENDAR_ID, event).execute();
            return "✅ Event added to Google Calendar!";
        } catch (Exception e) {
            return "❌ Failed to add event: " + e.getMessage();
        }
    }

    public String listEvents() {
        try {
            Events events = calendar.events().list(CALENDAR_ID)
                    .setTimeMin(new com.google.api.client.util.DateTime(new Date()))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            if (events.getItems().isEmpty()) return "📭 No upcoming events found.";

            return events.getItems().stream()
                    .map(e -> {
                        String start = Optional.ofNullable(e.getStart().getDateTime())
                                .map(com.google.api.client.util.DateTime::toStringRfc3339)
                                .orElse("All Day");
                        return "• " + e.getSummary() + " at " + start;
                    })
                    .collect(Collectors.joining("\n", "📅 Upcoming Events:\n", ""));
        } catch (Exception e) {
            return "❌ Failed to list events: " + e.getMessage();
        }
    }

    public String deleteEvent(String summary) {
        try {
            Events events = calendar.events().list(CALENDAR_ID)
                    .setQ(summary)
                    .setTimeMin(new com.google.api.client.util.DateTime(new Date()))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            for (Event event : events.getItems()) {
                if (event.getSummary().equalsIgnoreCase(summary)) {
                    calendar.events().delete(CALENDAR_ID, event.getId()).execute();
                    return "🗑️ Deleted event: " + summary;
                }
            }
            return "⚠️ No matching event found.";
        } catch (Exception e) {
            return "❌ Failed to delete event: " + e.getMessage();
        }
    }

    private Date parseDate(String input) {
        List<Date> dates = new Parser().parse(input).stream()
                .map(r -> r.getDates().get(0))
                .toList();
        return dates.isEmpty() ? null : dates.get(0);
    }
}
