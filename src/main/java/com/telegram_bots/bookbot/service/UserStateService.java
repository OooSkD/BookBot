package com.telegram_bots.bookbot.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserStateService {
    private final Map<Long, Boolean> waitingForBookTitle = new HashMap<>();

    public void setWaitingForBookTitle(Long userId, boolean isWaiting) {
        waitingForBookTitle.put(userId, isWaiting);
    }

    public boolean isWaitingForBookTitle(Long userId) {
        return waitingForBookTitle.getOrDefault(userId, false);
    }
}
