package com.telegram_bots.bookbot.model.dto;

public record Statistics(
        int todayBooks,
        int todayPages,
        int monthBooks,
        int monthPages,
        int yearBooks,
        int yearPages,
        String biggestBookTitle,
        int biggestBookPages
) {}
