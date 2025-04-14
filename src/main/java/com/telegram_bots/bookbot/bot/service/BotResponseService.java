package com.telegram_bots.bookbot.bot.service;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.service.BookService;
import com.telegram_bots.bookbot.service.LitresService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
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

        if (userStateService.isWaitingForBookTitle(chatId)) {
            userStateService.setWaitingForBookTitle(chatId, false);
            List<LitresBookDto> books = litresService.searchBooks(messageText);
            books = books.stream().limit(maxCountBooks).collect(Collectors.toList());
            userStateService.saveSearchResults(chatId, books);
            return List.of(messageService.buildBookSearchResults(chatId, books));
        }

        return List.of( messageService.buildUnknownCommandMessage(chatId));
    }

    public List<SendMessage> handleCallbackQuery(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        String command = data.contains(":") ? data.split(":")[0] : data;

        switch (command) {
            case "add_book" -> {
                userStateService.setWaitingForBookTitle(chatId, true);
                return List.of(messageService.buildRequestBookTitleMessage(chatId));
            }
            case "cancel" -> {
                //TODO: вынести в отдельны метод и поменять название command чтобы это кнопка отвечала только за отмену поиска книг
                // и вывести стартовую страницу после отмены
                userStateService.setWaitingForBookTitle(chatId, false);
                userStateService.clearSearchResults(chatId);
                return List.of(messageService.buildCancelledMessage(chatId));
            }
            case "show_books" -> {
                return List.of(buildBookListMessage(chatId));
            }
            case "select_book" -> {
                return handleBookSelection(chatId, data);
            }
            default -> {
                return List.of(messageService.buildUnknownCallbackMessage(chatId));
            }
        }
    }

    public DeleteMessage handleDeleteMessage(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String data = update.getCallbackQuery().getData();

        if (data.contains("select_book") || data.equals("cancel")) {
            return new DeleteMessage(chatId.toString(), messageId);
        }

        return null;
    }

    private SendMessage buildBookListMessage(Long chatId) {
        List<Book> books = bookService.getAllBooksOfUser(chatId);
        return messageService.buildBookListMessage(chatId, books);
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
}

