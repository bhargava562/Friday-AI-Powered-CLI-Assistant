package com.friday.ai_assistant.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MailTool {

    @Autowired
    private JavaMailSender mailSender;

    public String send(String prompt) {
        try {
            String to = extractBetween(prompt, "mail to", "subject:").trim().replace("\"", "");
            String subject = extractBetween(prompt, "subject:", "description:").trim().replace("\"", "");
            String body = prompt.substring(prompt.indexOf("description:") + 12).trim().replace("\"", "");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            return "📧 Mail sent successfully to " + to;
        } catch (Exception e) {
            return "❌ Failed to send email: " + e.getMessage();
        }
    }

    private String extractBetween(String text, String start, String end) {
        int startIndex = text.indexOf(start) + start.length();
        int endIndex = text.indexOf(end);
        return text.substring(startIndex, endIndex);
    }
}

