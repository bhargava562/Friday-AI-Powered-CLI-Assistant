package com.friday.ai_assistant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.friday.ai_assistant.model.Reminder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ReminderService {

    private static final File jsonFile = new File("D:/FRIDAY/ai-assistant/target/temp/reminders.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Reminder> reminders = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ReminderService() {
        loadReminders();
        startScheduler();
    }

    public void addReminder(String message, String time, boolean daily) {
        Reminder reminder = new Reminder(message.trim(), time.trim(), daily);
        reminders.add(reminder);
        saveReminders();
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void clearAllReminders() {
        reminders.clear();
        saveReminders();
    }

    public boolean removeReminder(String message, String time) {
        boolean removed = reminders.removeIf(r ->
                r.getMessage().equalsIgnoreCase(message.trim()) &&
                        r.getTime().equalsIgnoreCase(time.trim()));
        if (removed) saveReminders();
        return removed;
    }

    public boolean removeReminderAtTime(String time) {
        boolean removed = reminders.removeIf(r -> r.getTime().equalsIgnoreCase(time.trim()));
        if (removed) saveReminders();
        return removed;
    }

    private void loadReminders() {
        if (jsonFile.exists()) {
            try {
                if (jsonFile.length() == 0) return; // prevent parse error on empty file
                List<Reminder> saved = mapper.readValue(jsonFile, new TypeReference<>() {});
                reminders.addAll(saved);
            } catch (IOException e) {
                System.err.println("[ReminderService] Failed to load reminders: " + e.getMessage());
            }
        }
    }

    private void saveReminders() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, reminders);
        } catch (IOException e) {
            System.err.println("[ReminderService] Failed to save reminders: " + e.getMessage());
        }
    }

    private void startScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                LocalTime now = LocalTime.now().withSecond(0).withNano(0);
                Iterator<Reminder> iterator = reminders.iterator();

                while (iterator.hasNext()) {
                    Reminder reminder = iterator.next();
                    if (LocalTime.parse(reminder.getTime()).equals(now)) {
                        showNotification(reminder.getMessage());

                        if (!reminder.isDaily()) {
                            iterator.remove();
                            saveReminders();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[ReminderService] Scheduler error: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void showNotification(String message) {
        try {
            String powershellCommand = String.format(
                    "powershell -ExecutionPolicy Bypass -Command \"New-BurntToastNotification -Text 'Friday Reminder', '%s'\"",
                    message.replace("\"", "'")
            );
            Runtime.getRuntime().exec(powershellCommand);
        } catch (IOException e) {
            System.err.println("[ReminderService] Failed to send notification: " + e.getMessage());
        }
    }
}
