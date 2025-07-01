package com.friday.ai_assistant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.friday.ai_assistant.model.Reminder;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ReminderService {

    private final File jsonFile = new File("reminders.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Reminder> reminders = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ReminderService() {
        loadReminders();
        startScheduler();
    }

    public void addReminder(String message, String time, boolean daily) {
        Reminder reminder = new Reminder(message, time, daily);
        reminders.add(reminder);
        saveReminders();
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public boolean removeReminder(String message, String time) {
        boolean removed = reminders.removeIf(r ->
                r.getMessage().equalsIgnoreCase(message.trim()) &&
                        r.getTime().equals(time.trim())
        );
        if (removed) saveReminders();
        return removed;
    }

    private void loadReminders() {
        if (jsonFile.exists()) {
            try {
                List<Reminder> saved = mapper.readValue(jsonFile, new TypeReference<>() {});
                reminders.addAll(saved);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveReminders() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, reminders);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAllReminders() {
        reminders.clear();
        saveReminders();
    }

    public boolean removeReminderAtTime(String time) {
        boolean removed = reminders.removeIf(r -> r.getTime().equals(time));
        if (removed) saveReminders();
        return removed;
    }

    private void startScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
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
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void showNotification(String message) {
        try {
            String powershellCommand = String.format(
                    "powershell -Command \"New-BurntToastNotification -Text 'Friday Reminder', '%s'\"",
                    message.replace("\"", "'")  // escape double quotes
            );
            Runtime.getRuntime().exec(powershellCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

