package com.telegram_bots.bookbot.bot.service;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import com.telegram_bots.bookbot.model.session.UserSession;
import com.telegram_bots.bookbot.model.session.enums.UserState;
import org.springframework.stereotype.Service;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateService {

    //ключ - chatId
    private final Map<Long, UserSession> userSessions = new ConcurrentHashMap<>();

    private UserSession getSession(Long userId) {
        return userSessions.computeIfAbsent(userId, id -> new UserSession());
    }

    public void saveSearchResults(Long userId, List<LitresBookDto> results) {
        getSession(userId).setSearchResults(results);
    }

    public List<LitresBookDto> getSearchResults(Long userId) {
        return getSession(userId).getSearchResults();
    }

    public void clearSearchResults(Long userId) {
        getSession(userId).setSearchResults(Collections.emptyList());
    }

    public int getCurrentPage(Long chatId) {
        return getSession(chatId).getCurrentPage();
    }

    public void setCurrentPage(Long chatId, int page) {
        getSession(chatId).setCurrentPage(page);
    }

    public void incrementPage(Long chatId) {
        setCurrentPage(chatId, getCurrentPage(chatId) + 1);
    }

    public void decrementPage(Long chatId) {
        setCurrentPage(chatId, Math.max(0, getCurrentPage(chatId) - 1));
    }

    public BookStatus getBookStatusFilter(Long chatId) {
        return getSession(chatId).getBookStatusFilter();
    }

    public void setBookStatusFilter(Long chatId, BookStatus status) {
        getSession(chatId).setBookStatusFilter(status);
    }

    public UserState getState(Long chatId) {
        return getSession(chatId).getState();
    }
    public void setState(Long chatId, UserState state) {
        getSession(chatId).setState(state);
    }

    public void setBookIdForChange(Long chatId, Long bookId) {
        getSession(chatId).setBookIdForChange(bookId);
    }

    public Long getBookIdForChange(Long chatId) {
        return getSession(chatId).getBookIdForChange();
    }

}
