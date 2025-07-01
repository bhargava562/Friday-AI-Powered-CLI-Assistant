package com.friday.ai_assistant.cli;

import com.friday.ai_assistant.service.FridayService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

@Component
public class FridayCommandLineRunner implements CommandLineRunner {

    private final FridayService fridayService;

    public FridayCommandLineRunner(FridayService fridayService) {
        this.fridayService = fridayService;
    }

    @Override
    public void run(String... args) throws Exception {
        String logo = Files.readString(Paths.get("src/main/resources/friday-logo.txt"));
        System.out.println(logo);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(">>> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("bye")) {
                System.out.println("Friday: Goodbye Bhargava. Stay safe 👋");
                break;
            }

            String response = fridayService.getResponse(input);
            System.out.println("Friday: " + response);
        }
    }
}