package com.friday.ai_assistant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.friday.ai_assistant.model.Reminder;

import java.awt.*;
import java.io.File;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;

public class ReminderRunner {

    public static void main(String[] args) throws Exception {
        File jsonFile = new File("reminders.json");
        ObjectMapper mapper = new ObjectMapper();

        if (!jsonFile.exists()) return;

        List<Reminder> reminders = mapper.readValue(jsonFile, new TypeReference<>() {});
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        boolean changed = false;

        Iterator<Reminder> iterator = reminders.iterator();
        while (iterator.hasNext()) {
            Reminder reminder = iterator.next();
            LocalTime reminderTime = LocalTime.parse(reminder.getTime());

            if (reminderTime.equals(now)) {
                showNotification(reminder.getMessage());

                if (!reminder.isDaily()) {
                    iterator.remove();
                    changed = true;
                }
            }
        }

        if (changed) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, reminders);
        }
    }

    private static void showNotification(String message) {
        try {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
                TrayIcon trayIcon = new TrayIcon(image, "Friday");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
                trayIcon.displayMessage("Friday Reminder", message, TrayIcon.MessageType.INFO);
            } else {
                String ps = String.format("powershell -Command \"New-BurntToastNotification -Text 'Friday Reminder', '%s'\"", message);
                Runtime.getRuntime().exec(ps);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
