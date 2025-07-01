package com.friday.ai_assistant.cli;

import com.friday.ai_assistant.service.FridayService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class FridayCommandLineRunner implements CommandLineRunner {

    private final FridayService fridayService;

    // ANSI color codes
    private static final String HACKER_GREEN = "\u001B[32m";
    private static final String DARK_GREEN = "\u001B[92m";
    private static final String RESET = "\u001B[0m";

    public FridayCommandLineRunner(FridayService fridayService) {
        this.fridayService = fridayService;
    }

    @Override
    public void run(String... args) throws Exception {
        // ✅ Load logo from classpath, compatible with jar
        String logo;
        try (var in = getClass().getClassLoader().getResourceAsStream("friday-logo.txt");
             var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            logo = reader.lines().collect(Collectors.joining("\n"));
        }

        System.out.println(HACKER_GREEN + logo + RESET);
        System.out.println(HACKER_GREEN + "Hello Bhargava, how can I help you today?\n" + RESET);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(DARK_GREEN + ">>> " + RESET);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("bye")) {
                System.out.println(HACKER_GREEN + "Friday: Goodbye Bhargava. Stay safe 👋" + RESET);
                System.exit(0);
            }

            String response = fridayService.getResponse(input);
            System.out.println(HACKER_GREEN + "Friday: " + response + RESET);
        }
    }
}
