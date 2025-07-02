package com.friday.ai_assistant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.friday.ai_assistant.model.Reminder;

import java.io.File;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReminderRunner {

    private static final File jsonFile = new File("D:/FRIDAY/ai-assistant/target/temp/reminders.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                List<Reminder> reminders = loadReminders();
                LocalTime now = LocalTime.now().withSecond(0).withNano(0);
                LocalTime oneMinuteAgo = now.minusMinutes(1);

                reminders.removeIf(r -> {
                    try {
                        LocalTime reminderTime = LocalTime.parse(r.getTime());
                        if (!reminderTime.isAfter(now) && !reminderTime.isBefore(oneMinuteAgo)) {
                            notify(r.getMessage());
                            return !r.isDaily(); // remove only if not daily
                        }
                    } catch (Exception ignored) {}
                    return false;
                });

                saveReminders(reminders);
            } catch (Exception ignored) {}
        }, 0, 1, TimeUnit.MINUTES);
    }

    private static void notify(String message) {
        try {
            String command = String.format(
                    "powershell -ExecutionPolicy Bypass -Command \"New-BurntToastNotification -Text 'Friday Reminder', '%s'\"",
                    message.replace("\"", "'")
            );
            Runtime.getRuntime().exec(command);
        } catch (Exception ignored) {}
    }

    private static List<Reminder> loadReminders() {
        try {
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                return new CopyOnWriteArrayList<>();
            }
            List<Reminder> loaded = mapper.readValue(jsonFile, new TypeReference<>() {});
            return new CopyOnWriteArrayList<>(loaded);
        } catch (Exception ignored) {
            return new CopyOnWriteArrayList<>();
        }
    }

    private static void saveReminders(List<Reminder> reminders) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, reminders);
        } catch (Exception ignored) {}
    }
}
