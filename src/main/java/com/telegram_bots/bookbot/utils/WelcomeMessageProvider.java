package com.telegram_bots.bookbot.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Component
public class WelcomeMessageProvider {
    private final List<String> welcomeMessages = new ArrayList<>();
    private final Random random = new Random();

    @PostConstruct
    public void loadMessages() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("messages/welcome_messages.txt")),
                        StandardCharsets.UTF_8
                )
        )) {
            StringBuilder message = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("---")) {
                    welcomeMessages.add(message.toString().trim());
                    message.setLength(0);
                } else {
                    message.append(line).append("\n");
                }
            }

            if (message.length() > 0) {
                welcomeMessages.add(message.toString().trim());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load welcome messages", e);
        }
    }

    public String getRandomMessage() {
        if (welcomeMessages.isEmpty()) {
            return "Привет! Я бот для отслеживания книг 📚";
        }
        return welcomeMessages.get(random.nextInt(welcomeMessages.size()));
    }
}
