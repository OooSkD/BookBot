package com.telegram_bots.bookbot.bot.service;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.service.BookService;
import com.telegram_bots.bookbot.service.LitresService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

    public SendMessage handleTextMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        if ("/start".equals(messageText)) {
            return messageService.buildWelcomeMessage(chatId);
        }

        if (userStateService.isWaitingForBookTitle(chatId)) {
            userStateService.setWaitingForBookTitle(chatId, false);
            List<LitresBookDto> books = litresService.searchBooks(messageText);
            books = books.stream().limit(maxCountBooks).collect(Collectors.toList());
            return messageService.buildBookSearchResults(chatId, books);
        }

        return messageService.buildUnknownCommandMessage(chatId);
    }

    public SendMessage handleCallbackQuery(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        String command = data.contains(":") ? data.split(":")[0] : data;

        switch (command) {
            case "add_book":
                userStateService.setWaitingForBookTitle(chatId, true);
                return messageService.buildRequestBookTitleMessage(chatId);

            case "cancel":
                userStateService.setWaitingForBookTitle(chatId, false);
                return messageService.buildCancelledMessage(chatId);

            case "show_books":
                return buildBookListMessage(chatId);

            case "select_book":
                return handleBookSelection(chatId, data);

            default:
                return messageService.buildUnknownCallbackMessage(chatId);
        }
    }

    private SendMessage buildBookListMessage(Long chatId) {
        List<Book> books = bookService.getAllBooksOfUser(chatId);
        return messageService.buildBookListMessage(chatId, books);
    }

    private SendMessage handleBookSelection(Long chatId, String data) {
        //TODO: переделать. нужно доставать из кэша
        //Пример: select_book:название|0
        String info = data.substring("select_book:".length());
        String[] parts = info.split("\\|");

        String searchTitle = parts[0];
        int index = Integer.parseInt(parts[1]);

        List<LitresBookDto> foundBooks = litresService.searchBooks(searchTitle);

        if (index >= foundBooks.size()) {
            return messageService.buildBookNotFoundByIndexMessage(chatId);
        }

        LitresBookDto selectedBook = foundBooks.get(index);
        bookService.addBook(chatId, selectedBook.getTitle(), selectedBook.getAuthor());

        return messageService.buildBookAddedMessage(chatId, selectedBook.getTitle());
    }
}

