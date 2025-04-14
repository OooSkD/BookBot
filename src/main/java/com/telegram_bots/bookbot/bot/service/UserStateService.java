package com.telegram_bots.bookbot.bot.service;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserStateService {
    private final Map<Long, Boolean> waitingForBookTitle = new HashMap<>();
    private final Map<Long, List<LitresBookDto>> searchResultsCache = new HashMap<>();

    public void setWaitingForBookTitle(Long userId, boolean isWaiting) {
        waitingForBookTitle.put(userId, isWaiting);
    }

    public boolean isWaitingForBookTitle(Long userId) {
        return waitingForBookTitle.getOrDefault(userId, false);
    }

    public void saveSearchResults(Long userId, List<LitresBookDto> results) {
        searchResultsCache.put(userId, results);
    }

    public List<LitresBookDto> getSearchResults(Long userId) {
        return searchResultsCache.getOrDefault(userId, Collections.emptyList());
    }

    public void clearSearchResults(Long userId) {
        searchResultsCache.remove(userId);
    }
}
