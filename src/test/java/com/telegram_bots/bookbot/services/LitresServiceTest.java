package com.telegram_bots.bookbot.services;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import com.telegram_bots.bookbot.service.LitresService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LitresServiceTest {
    @Mock
    private Document documentMock;

    @Mock
    private Element bookElementMock;

    private LitresService litresService;

    @BeforeEach
    void setUp() {
        litresService = Mockito.spy(new LitresService());
    }

    @Test
    void testSearchBooks_Success() throws IOException {
        when(documentMock.select(".art-item")).thenReturn(new Elements(bookElementMock));

        Element titleElement = mock(Element.class);
        when(titleElement.text()).thenReturn("Book Title");

        Element authorElement = mock(Element.class);
        when(authorElement.text()).thenReturn("Author Name");

        when(bookElementMock.select(".art-item__name a")).thenReturn(new Elements(titleElement));
        when(bookElementMock.select(".art-item__author a")).thenReturn(new Elements(authorElement));

        doReturn(documentMock).when(litresService).fetchDocument(anyString());

        List<LitresBookDto> books = litresService.searchBooks("some query");

        assertEquals(1, books.size());
        assertEquals("Book Title", books.get(0).getTitle());
        assertEquals("Author Name", books.get(0).getAuthor());
    }

    @Test
    void testSearchBooks_EmptyTitle() throws IOException {
        when(documentMock.select(".art-item")).thenReturn(new Elements(bookElementMock));

        Element titleElement = mock(Element.class);
        when(titleElement.text()).thenReturn("");

        Element authorElement = mock(Element.class);
        when(authorElement.text()).thenReturn("Author Name");

        when(bookElementMock.select(".art-item__name a")).thenReturn(new Elements(titleElement));
        when(bookElementMock.select(".art-item__author a")).thenReturn(new Elements(authorElement));

        doReturn(documentMock).when(litresService).fetchDocument(anyString());

        List<LitresBookDto> books = litresService.searchBooks("some query");

        assertTrue(books.isEmpty());
    }

    @Test
    void testSearchBooks_Exception() throws IOException {
        doThrow(new IOException("Network error")).when(litresService).fetchDocument(anyString());

        List<LitresBookDto> books = litresService.searchBooks("some query");

        assertTrue(books.isEmpty());
    }
}
