package com.telegram_bots.bookbot.bot.service;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserStateServiceTest {
    private UserStateService userStateService;

    @BeforeEach
    void setUp() {
        userStateService = new UserStateService();
    }

    @Test
    void testSetAndCheckWaitingForBookTitle() {
        long chatId = 123L;

        assertFalse(userStateService.isWaitingForBookTitle(chatId));

        userStateService.setWaitingForBookTitle(chatId, true);
        assertTrue(userStateService.isWaitingForBookTitle(chatId));

        userStateService.setWaitingForBookTitle(chatId, false);
        assertFalse(userStateService.isWaitingForBookTitle(chatId));
    }

    @Test
    void testSaveAndRetrieveSearchResults() {
        long chatId = 456L;
        LitresBookDto book = new LitresBookDto("Название", "Автор");

        userStateService.saveSearchResults(chatId, List.of(book));

        List<LitresBookDto> results = userStateService.getSearchResults(chatId);
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Название", results.get(0).getTitle());
        assertEquals("Автор", results.get(0).getAuthor());
    }

    @Test
    void testClearSearchResults() {
        long chatId = 789L;
        LitresBookDto book = new LitresBookDto("Book to clear", "Автор");

        userStateService.saveSearchResults(chatId, List.of(book));
        assertFalse(userStateService.getSearchResults(chatId).isEmpty());

        userStateService.clearSearchResults(chatId);
        assertTrue(userStateService.getSearchResults(chatId).isEmpty());
    }
}
