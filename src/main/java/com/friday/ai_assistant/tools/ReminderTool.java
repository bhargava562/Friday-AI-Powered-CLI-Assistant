package com.friday.ai_assistant.tools;

import com.friday.ai_assistant.model.Reminder;
import com.friday.ai_assistant.service.ReminderService;
import com.joestelmach.natty.Parser;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Component
public class ReminderTool {

    private final ReminderService reminderService;

    public ReminderTool(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    public String setReminder(String input) {
        try {
            Parser parser = new Parser();
            List<Date> dates = parser.parse(input).stream()
                    .flatMap(g -> g.getDates().stream())
                    .toList();

            if (dates.isEmpty()) {
                return "Sorry, I couldn't understand the time in your reminder.";
            }

            String message = extractMessage(input);
            LocalTime time = dates.get(0).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalTime()
                    .withSecond(0)
                    .withNano(0);

            boolean daily = input.toLowerCase().contains("daily");

            reminderService.addReminder(message, time.toString(), daily);
            return (daily ? "Daily" : "One-time") + " reminder set: " + message + " at " + time;
        } catch (Exception e) {
            return "Failed to set reminder: " + e.getMessage();
        }
    }

    public String listReminders() {
        List<Reminder> active = reminderService.getReminders();
        if (active.isEmpty()) {
            return "You have no active reminders.";
        }

        StringBuilder sb = new StringBuilder("Here are your active reminders:\n");
        for (Reminder r : active) {
            sb.append("- ").append(r.getMessage())
                    .append(" at ").append(r.getTime())
                    .append(r.isDaily() ? " (daily)" : " (once)")
                    .append("\n");
        }
        return sb.toString();
    }

    public String cancelReminder(String input) {
        String lower = input.toLowerCase();

        // Cancel all
        if (lower.contains("all")) {
            reminderService.clearAllReminders();
            return "All reminders cleared.";
        }

        // Extract time
        Parser parser = new Parser();
        List<Date> dates = parser.parse(input).stream()
                .flatMap(g -> g.getDates().stream())
                .toList();

        if (dates.isEmpty()) {
            return "Couldn't find a time in your cancellation request.";
        }

        LocalTime targetTime = dates.get(0).toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime()
                .withSecond(0)
                .withNano(0);

        boolean removed = reminderService.removeReminderAtTime(targetTime.toString());

        if (removed) {
            return "Reminder at " + targetTime + " deleted.";
        } else {
            return "No reminder found at " + targetTime + ".";
        }
    }

    private String extractMessage(String input) {
        String lowered = input.toLowerCase();
        if (lowered.startsWith("remind me to ")) {
            return input.substring(13).replaceAll(" at .*| in .*", "").trim();
        } else if (lowered.startsWith("remind me ")) {
            return input.substring(10).replaceAll(" at .*| in .*", "").trim();
        }
        return input;
    }
}
