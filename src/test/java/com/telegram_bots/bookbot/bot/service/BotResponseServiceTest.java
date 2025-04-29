package com.telegram_bots.bookbot.bot.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import com.telegram_bots.bookbot.model.session.enums.UserState;
import com.telegram_bots.bookbot.service.BookService;
import com.telegram_bots.bookbot.service.LitresService;
import com.telegram_bots.bookbot.utils.WelcomeMessageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class BotResponseServiceTest {

    @Mock private UserStateService userStateService;
    @Mock private LitresService litresService;
    @Mock private BookService bookService;
    @Mock private Update update;

    private BotResponseService botResponseService;
    private Book book = new Book("Title", "Author", BookStatus.PLANNED);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MessageService messageService = new MessageService(new WelcomeMessageProvider());
        botResponseService = spy(new BotResponseService(userStateService, litresService, messageService, bookService));

    }

    @Test
    void testHandleTextMessageStartCommand() {
        Long chatId = 123L;
        String messageText = "/start";
        Message message = mock(Message.class);

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(messageText);

        List<SendMessage> result = botResponseService.handleTextMessage(update);
        assertEquals(1, result.size());
    }

    @Test
    void testHandleTextMessageWaitingForTitle() {
        Long chatId = 123L;
        String messageText = "Book Title";
        Message message = mock(Message.class);

        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(messageText);
        when(userStateService.getState(chatId)).thenReturn(UserState.WAITING_FOR_TITLE);
        when(litresService.searchBooks(messageText)).thenReturn(List.of(new LitresBookDto("Book Title", "Author Name")));

        List<SendMessage> result = botResponseService.handleTextMessage(update);

        assertEquals(1, result.size());
    }

    @Test
    void testHandlePageInputValid() {
        Long chatId = 123L;
        String messageText = "5";
        Book book = mock(Book.class);

        when(bookService.getBookById(anyLong())).thenReturn(book);
        when(book.getCurrentPage()).thenReturn(null);
        when(book.getStatus()).thenReturn(BookStatus.PLANNED);

        List<SendMessage> result = botResponseService.handlePageInput(chatId, messageText);

        assertEquals(2, result.size());
        verify(bookService).updatePage(book, 5);
    }

    @Test
    void testHandleRatingInputValid() {
        Long chatId = 123L;
        String messageText = "8";
        Book book = mock(Book.class);

        when(bookService.getBookById(anyLong())).thenReturn(book);
        when(book.getStatus()).thenReturn(BookStatus.READ);

        List<SendMessage> result = botResponseService.handleRatingInput(chatId, messageText);

        assertEquals(2, result.size());
        verify(bookService).updateRating(book, 8);
    }

    @Test
    void testHandleDeleteMessage() {
        Long chatId = 123L;
        Integer messageId = 456;
        String data = "cancel";

        Message message = mock(Message.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getMessageId()).thenReturn(messageId);
        when(callbackQuery.getData()).thenReturn(data);

        DeleteMessage result = botResponseService.handleDeleteMessage(update);

        assertNotNull(result);
        assertEquals(chatId.toString(), result.getChatId());
        assertEquals(messageId, result.getMessageId());
    }

    @Test
    void testExtractBookId() {
        String data = "delete_book:42";
        Long result = botResponseService.extractBookId(data);
        assertEquals(42L, result);
    }

    @Test
    void testHandleCancelUpdateBook() {
        Long chatId = 123L;
        Long bookId = 1L;

        when(userStateService.getBookIdForChange(chatId)).thenReturn(bookId);
        when(bookService.getBookById(bookId)).thenReturn(book);

        List<SendMessage> result = botResponseService.handleCancelUpdateBook(chatId);

        assertEquals(2, result.size());
        assertNotNull(result.get(0).getText());
    }

    @Test
    void testHandleChangeStatus() {
        Long chatId = 123L;
        List<SendMessage> result = botResponseService.handleChangeStatus(chatId);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getText().contains("Выбери новый статус"));
    }

    @Test
    void testHandleSetStatusCallback() {
        Long chatId = 123L;
        Long bookId = 1L;
        String data = "set_status:READ";

        when(userStateService.getBookIdForChange(chatId)).thenReturn(bookId);
        when(bookService.getBookById(bookId)).thenReturn(book);

        List<SendMessage> result = botResponseService.handleSetStatusCallback(chatId, data);

        assertEquals(2, result.size());
        assertNotNull(result.get(0).getText());
    }

    @Test
    void testHandleBookSelection() {
        Long chatId = 123L;
        String data = "select_book:0";

        LitresBookDto bookDto = new LitresBookDto("Title", "Author");

        when(userStateService.getSearchResults(chatId)).thenReturn(List.of(bookDto));

        List<SendMessage> result = botResponseService.handleBookSelection(chatId, data);

        assertEquals(2, result.size());
    }

    @Test
    void testBuildStatusFilterButtons() {
        Long chatId = 123L;
        SendMessage result = botResponseService.buildStatusFilterButtons(chatId);

        assertNotNull(result.getText());
        assertTrue(result.getText().contains("статус"));
    }

    @Test
    void testIsValidPageInput() {
        assertTrue(botResponseService.isValidPageInput("123"));
        assertFalse(botResponseService.isValidPageInput("-1"));
        assertFalse(botResponseService.isValidPageInput("abc"));
        assertFalse(botResponseService.isValidPageInput("0"));
    }

    @Test
    void testHandleBookTitle() {
        long chatId = 123L;
        String title = "BookTitle";

        List<SendMessage> result = botResponseService.handleBookTitle(chatId, title);

        assertEquals(1, result.size());
    }

    @Test
    void testBuildBookListMessage_callbackDataGeneratedCorrectly() {
        Long chatId = 123L;
        int currentPage = 0;
        BookStatus filter = BookStatus.READING;

        Book book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Книга 1");
        book1.setStatus(BookStatus.READING);

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Книга 2");
        book2.setStatus(BookStatus.READING);

        when(bookService.getAllBooksOfUser(chatId)).thenReturn(List.of(book1, book2));
        when(userStateService.getCurrentPage(chatId)).thenReturn(currentPage);
        when(userStateService.getBookStatusFilter(chatId)).thenReturn(filter);

        SendMessage result = botResponseService.buildBookListMessage(chatId);

        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) result.getReplyMarkup();
        List<List<InlineKeyboardButton>> rows = markup.getKeyboard();

        InlineKeyboardButton button1 = rows.get(0).get(0);
        InlineKeyboardButton button2 = rows.get(1).get(0);

        assertEquals("Книга 1", button1.getText());
        assertEquals("manage_book:1", button1.getCallbackData());

        assertEquals("Книга 2", button2.getText());
        assertEquals("manage_book:2", button2.getCallbackData());
    }

    @Nested
    class HandleCallbackQueryTests {

        @Test
        void testHandleCallbackQuery_AddBook() {
            Long chatId = 123L;
            String data = "add_book";

            Message message = mock(Message.class);
            CallbackQuery callbackQuery = mock(CallbackQuery.class);

            when(update.getCallbackQuery()).thenReturn(callbackQuery);
            when(callbackQuery.getMessage()).thenReturn(message);
            when(message.getChatId()).thenReturn(chatId);
            when(callbackQuery.getData()).thenReturn(data);

            List<SendMessage> result = botResponseService.handleCallbackQuery(update);

            assertEquals(1, result.size());
            verify(userStateService).setState(chatId, UserState.WAITING_FOR_TITLE);
        }

        @Test
        void testHandleCallbackQuery_setStatus() {
            Long chatId = 123L;
            String data = "set_status:READ";
            Long bookId = 1L;

            Message message = mock(Message.class);
            CallbackQuery callbackQuery = mock(CallbackQuery.class);

            when(update.getCallbackQuery()).thenReturn(callbackQuery);
            when(callbackQuery.getMessage()).thenReturn(message);
            when(message.getChatId()).thenReturn(chatId);
            when(callbackQuery.getData()).thenReturn(data);
            when(userStateService.getBookIdForChange(chatId)).thenReturn(bookId);
            when(bookService.getBookById(bookId)).thenReturn(book);

            List<SendMessage> result = botResponseService.handleCallbackQuery(update);

            assertEquals(2, result.size());
        }

        @Test
        void testHandleCallbackQuery_cancelAddedBook() {
            Long chatId = 123L;
            String data = "cancel_added_book";

            CallbackQuery callbackQuery = mock(CallbackQuery.class);
            Message message = mock(Message.class);

            when(update.getCallbackQuery()).thenReturn(callbackQuery);
            when(message.getChatId()).thenReturn(chatId);
            when(callbackQuery.getMessage()).thenReturn(message);
            when(callbackQuery.getData()).thenReturn(data);
            when(message.getChatId()).thenReturn(chatId);

            List<SendMessage> result = botResponseService.handleCallbackQuery(update);

            assertEquals(1, result.size());

            SendMessage actual = result.get(0);
            assertEquals(String.valueOf(chatId), actual.getChatId());
            assertTrue(actual.getText().contains("книжка") || actual.getText().contains("подождёт"));

            verify(userStateService).setState(chatId, UserState.NONE);
            verify(userStateService).clearSearchResults(chatId);
        }

        @Test
        void testHandleCallbackQuery_showBooks() {
            Long chatId = 123L;
            String data = "show_books";

            CallbackQuery callbackQuery = mock(CallbackQuery.class);
            Message message = mock(Message.class);

            when(update.getCallbackQuery()).thenReturn(callbackQuery);
            when(callbackQuery.getMessage()).thenReturn(message);
            when(callbackQuery.getData()).thenReturn(data);
            when(message.getChatId()).thenReturn(chatId);

            SendMessage expectedMessage = new SendMessage();
            doReturn(expectedMessage).when(botResponseService).buildBookListMessage(chatId);

            List<SendMessage> result = botResponseService.handleCallbackQuery(update);

            verify(userStateService).setBookIdForChange(chatId, null);
            assertEquals(List.of(expectedMessage), result);
        }


        @Test
        void testHandleCallbackQuery_selectBook() {
            Long chatId = 123L;
            String data = "select_book:1";

            CallbackQuery callbackQuery = mock(CallbackQuery.class);
            Message message = mock(Message.class);

            when(update.getCallbackQuery()).thenReturn(callbackQuery);
            when(callbackQuery.getMessage()).thenReturn(message);
            when(callbackQuery.getData()).thenReturn(data);
            when(message.getChatId()).thenReturn(chatId);

            List<SendMessage> expected = List.of(new SendMessage());
            doReturn(expected).when(botResponseService).handleBookSelection(chatId, data);

            List<SendMessage> result = botResponseService.handleCallbackQuery(update);

            assertEquals(expected, result);
        }

        @Test
        void testHandleCallbackQuery_deleteBook() {
            Long chatId = 123L;
            Long bookId = 42L;
            String data = "delete_book:" + bookId;

            CallbackQuery callbackQuery = mock(CallbackQuery.class);
            Message message = mock(Message.class);

            when(update.getCallbackQuery()).thenReturn(callbackQuery);
            when(callbackQuery.getMessage()).thenReturn(message);
            when(callbackQuery.getData()).thenReturn(data);
            when(message.getChatId()).thenReturn(chatId);

            List<SendMessage> result = botResponseService.handleCallbackQuery(update);

            verify(bookService).deleteBook(bookId);
        }
    }
}

