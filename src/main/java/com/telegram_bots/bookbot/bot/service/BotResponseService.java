package com.telegram_bots.bookbot.bot.service;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import com.telegram_bots.bookbot.model.session.enums.UserState;
import com.telegram_bots.bookbot.service.BookService;
import com.telegram_bots.bookbot.service.LitresService;
import com.telegram_bots.bookbot.utils.ButtonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BotResponseService {

    private final UserStateService userStateService;
    private final LitresService litresService;
    private final MessageService messageService;
    private final BookService bookService;

    @Value("${book.searchResult.maxCount}")
    private int maxCountBooks;

    public BotResponseService(UserStateService userStateService, LitresService litresService, MessageService messageService, BookService bookService) {
        this.userStateService = userStateService;
        this.litresService = litresService;
        this.messageService = messageService;
        this.bookService = bookService;
    }

    public List<SendMessage> handleTextMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        if ("/start".equals(messageText)) {
            return List.of(messageService.buildWelcomeMessage(chatId));
        }

        UserState state = userStateService.getState(chatId);

        return switch (state) {
            case WAITING_FOR_TITLE -> handleBookTitle(chatId, messageText);
            case WAITING_FOR_PAGE -> handlePageInput(chatId, messageText);
            case WAITING_FOR_RATING -> handleRatingInput(chatId, messageText);
            default -> List.of(messageService.buildUnknownCommandMessage(chatId));
        };
    }

    private List<SendMessage> handleBookTitle(Long chatId, String title) {
        List<LitresBookDto> books = litresService.searchBooks(title)
                .stream().limit(maxCountBooks).toList();

        if (books.isEmpty()) {
            return List.of(messageService.buildNoBooksFoundMessage(chatId));
        }

        userStateService.setState(chatId, UserState.NONE);
        userStateService.saveSearchResults(chatId, books);
        return List.of(messageService.buildBookSearchResults(chatId, books));
    }

    private Book getBookAndClearState(Long chatId) {
        userStateService.setState(chatId, UserState.NONE);
        Long bookId = userStateService.getBookIdForChange(chatId);
        return bookService.getBookById(bookId);
    }

    private List<SendMessage> handlePageInput(Long chatId, String messageText) {
        Book book = getBookAndClearState(chatId);
        int page = Integer.parseInt(messageText);
        bookService.updatePage(book, page);

        return messageService.buildUpdatedPageMessage(chatId, page, book);
    }

    private List<SendMessage> handleRatingInput(Long chatId, String messageText) {
        Book book = getBookAndClearState(chatId);
        int rating = Integer.parseInt(messageText);
        bookService.updateRating(book, rating);

        return messageService.buildUpdatedRatingMessage(chatId, rating, book);
    }

    public List<SendMessage> handleCallbackQuery(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        String command = data.contains(":") ? data.split(":")[0] : data;

        switch (command) {
            case "add_book" -> {
                userStateService.setState(chatId, UserState.WAITING_FOR_TITLE);
                return List.of(messageService.buildRequestBookTitleMessage(chatId));
            }
            case "cancel_added_book" -> {
                userStateService.setState(chatId, UserState.NONE);
                userStateService.clearSearchResults(chatId);
                return List.of(messageService.buildCancelledMessage(chatId));
            }
            case "show_books" -> {
                userStateService.setBookIdForChange(chatId, null);
                return List.of(buildBookListMessage(chatId));
            }
            case "select_book" -> {
                return handleBookSelection(chatId, data);
            }
            case "books_next_page" -> {
                userStateService.incrementPage(chatId);
                return List.of(buildBookListMessage(chatId));
            }
            case "books_prev_page" -> {
                userStateService.decrementPage(chatId);
                return List.of(buildBookListMessage(chatId));
            }
            case "change_filter" -> {
                return List.of(buildStatusFilterButtons(chatId));
            }
            case "filter_status_clear" -> {
                userStateService.setBookStatusFilter(chatId, null);
                return List.of(buildBookListMessage(chatId));
            }
            case "filter_by_status" -> {
                String statusKey = data.substring("filter_by_status:".length());
                BookStatus status = BookStatus.valueOf(statusKey);
                userStateService.setBookStatusFilter(chatId, status);
                return List.of(buildBookListMessage(chatId));
            }
            case "manage_book" -> {
                Long bookId = extractBookId(data);
                userStateService.setBookIdForChange(chatId, bookId);
                Optional<Book> optionalBook = bookService.getBookOptionalById(bookId);
                return List.of(messageService.buildBookMenuMessage(chatId, optionalBook));
            }
            case "change_status" -> {
                return handleChangeStatus(chatId);
            }
            case "update_page" -> {
                userStateService.setState(chatId, UserState.WAITING_FOR_PAGE);
                return List.of(messageService.buildRequestPageInputMessage(chatId));
            }
            case "rate_book" -> {
                userStateService.setState(chatId, UserState.WAITING_FOR_RATING);
                return List.of(messageService.buildRequestRatingInputMessage(chatId));
            }
            case "delete_book" -> {
                return handleDeleteBook(chatId, data);
            }

            case "set_status" -> {
                return handleSetStatusCallback(chatId, data);
            }
            default -> {
                return List.of(messageService.buildUnknownCallbackMessage(chatId));
            }
        }
    }

    public Long extractBookId(String data) {
        return Long.parseLong(data.split(":")[1]);
    }


    public DeleteMessage handleDeleteMessage(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String data = update.getCallbackQuery().getData();
        List<String> commandToDelete = List.of("cancel",
                "add_book",
                "books_prev_page",
                "books_next_page",
                "change_filter");
        if (data.contains("select_book") || commandToDelete.contains(data)) {
            return new DeleteMessage(chatId.toString(), messageId);
        }

        return null;
    }

    private List<SendMessage> handleBookSelection(Long chatId, String data) {
        int index = Integer.parseInt(data.substring("select_book:".length()));
        List<LitresBookDto> books = userStateService.getSearchResults(chatId);

        if (index >= books.size()) {
            return List.of(messageService.buildBookNotFoundByIndexMessage(chatId));
        }

        LitresBookDto selectedBook = books.get(index);
        bookService.addBook(chatId, selectedBook.getTitle(), selectedBook.getAuthor());
        userStateService.clearSearchResults(chatId);

        SendMessage addedMessage = messageService.buildBookAddedMessage(chatId, selectedBook.getTitle());
        SendMessage listMessage = buildBookListMessage(chatId);

        return List.of(addedMessage, listMessage);

    }

    public SendMessage buildBookListMessage(Long chatId) {
        List<Book> allBooks = bookService.getAllBooksOfUser(chatId);

        BookStatus filter = userStateService.getBookStatusFilter(chatId);
        int currentPage = userStateService.getCurrentPage(chatId);

        // Фильтрация
        List<Book> filteredBooks = (filter == null)
                ? allBooks
                : allBooks.stream()
                .filter(book -> book.getStatus() == filter)
                .collect(Collectors.toList());

        // Пагинация
        int pageSize = 10;
        int fromIndex = Math.min(currentPage * pageSize, filteredBooks.size());
        int toIndex = Math.min(fromIndex + pageSize, filteredBooks.size());
        List<Book> booksOnPage = filteredBooks.subList(fromIndex, toIndex);

        // Сборка текста
        String text = messageService.buildBooksText(booksOnPage, filter);

        // Сборка кнопок
        List<List<InlineKeyboardButton>> rows = new ArrayList<>(messageService.buildBookButtons(booksOnPage));

        List<InlineKeyboardButton> pagination = messageService.buildPaginationButtons(currentPage, filteredBooks.size(), pageSize);
        if (!pagination.isEmpty()) {
            rows.add(pagination);
        }

        rows.addAll(messageService.buildFilterAndAddButtons());

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);

        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(markup)
                .build();
    }

    private SendMessage buildStatusFilterButtons(Long chatId) {
        String text = "Выберите статус для фильтрации 📖";

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (BookStatus status : BookStatus.values()) {
            InlineKeyboardButton button = new InlineKeyboardButton(status.getDisplayNameRu());
            button.setCallbackData("filter_by_status:" + status.name());
            currentRow.add(button);

            if (currentRow.size() == 2) {
                rows.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }

        // последний ряд, если кол-во кнопок нечетно
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        // Кнопка "Показать все"
        InlineKeyboardButton clearFilter = new InlineKeyboardButton("📋 Показать все");
        clearFilter.setCallbackData("filter_status_clear");
        rows.add(List.of(clearFilter));

        // Кнопка "Назад"
        InlineKeyboardButton back = new InlineKeyboardButton("🔙 Назад");
        back.setCallbackData("show_books");
        rows.add(List.of(back));

        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rows).build())
                .build();
    }

    private List<SendMessage> handleChangeStatus(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = Arrays.stream(BookStatus.values())
                .map(status -> List.of(
                        ButtonUtils.createButton(status.getDisplayNameRu(), "set_status:" + status.name())
                ))
                .toList();

        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выбери новый статус книги:");
        message.setReplyMarkup(markup);

        return List.of(message);
    }

    private List<SendMessage> handleDeleteBook(Long chatId, String data) {
        Long bookId = extractBookId(data);
        bookService.deleteBook(bookId);
        return List.of(messageService.buildDeletedBookMessage(chatId));
    }

    private List<SendMessage> handleSetStatusCallback(Long chatId, String data) {
        String statusStr = data.replace("set_status:", "").trim();

        BookStatus status;
        try {
            status = BookStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return List.of(messageService.createSimpleMessage(chatId, "Неизвестный статус: " + statusStr));
        }

        Long bookId = userStateService.getBookIdForChange(chatId);
        if (bookId == null) {
            return List.of(messageService.createSimpleMessage(chatId, "Книга не выбрана для изменения статуса"));
        }

        Book book = bookService.getBookById(bookId);
        bookService.updateStatus(book, status);

        return messageService.buildUpdatedStatusMessage(chatId, status, book);
    }
}

