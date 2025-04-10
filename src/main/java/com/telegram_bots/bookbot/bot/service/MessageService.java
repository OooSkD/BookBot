package com.telegram_bots.bookbot.bot.service;


import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import com.telegram_bots.bookbot.model.entities.Book;

import java.util.*;

@Service
public class MessageService {

    public SendMessage buildWelcomeMessage(Long chatId) {
        return buildWelcomeMessage(String.valueOf(chatId));
    }

    public SendMessage buildWelcomeMessage(String chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(InlineKeyboardButton.builder()
                .text("Посмотреть мои книги")
                .callbackData("show_books")
                .build());

        row.add(InlineKeyboardButton.builder()
                .text("Добавить книгу")
                .callbackData("add_book")
                .build());

        markup.setKeyboard(List.of(row));

        return SendMessage.builder()
                .chatId(chatId)
                .text("Привет! Я бот для отслеживания книг 📚")
                .replyMarkup(markup)
                .build();
    }


    public SendMessage buildRequestBookTitleMessage(Long chatId) {
        return buildRequestBookTitleMessage(String.valueOf(chatId));
    }

    public SendMessage buildRequestBookTitleMessage(String chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("Введите название книги:")
                .build();
    }


    public SendMessage buildBookSearchResults(Long chatId, List<LitresBookDto> books) {
        return buildBookSearchResults(String.valueOf(chatId), books);
    }

    public SendMessage buildBookSearchResults(String chatId, List<LitresBookDto> books) {
        if (books.isEmpty()) {
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Ничего не нашлось 😢 Введите название ещё раз.")
                    .build();
        }

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = 0; i < books.size(); i++) {
            LitresBookDto book = books.get(i);
            String text = book.getTitle() + " - " + book.getAuthor();
            String callback = "select_book:" + book.getTitle() + "|" + i;

            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(text)
                    .callbackData(callback)
                    .build();

            rows.add(List.of(button));
        }

        // Добавляем кнопку отмены
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("❌ Отмена")
                        .callbackData("cancel")
                        .build()
        ));

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(rows).build();

        return SendMessage.builder()
                .chatId(chatId)
                .text("Выберите книгу:")
                .replyMarkup(markup)
                .build();
    }

    public SendMessage buildCancelledMessage(Long chatId) {
        return buildCancelledMessage(String.valueOf(chatId));
    }

    public SendMessage buildCancelledMessage(String chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("Добавление книги отменено.")
                .build();
    }

    public SendMessage buildUnknownCommandMessage(Long chatId) {
        return buildUnknownCommandMessage(String.valueOf(chatId));
    }

    public SendMessage buildUnknownCommandMessage(String chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("Неизвестная команда. Попробуйте /start")
                .build();
    }

    public SendMessage buildUnknownCallbackMessage(Long chatId) {
        return buildUnknownCallbackMessage(String.valueOf(chatId));
    }

    public SendMessage buildUnknownCallbackMessage(String chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("Неизвестное действие. Попробуйте снова.")
                .build();
    }

    public SendMessage buildBookListMessage(Long chatId, List<Book> books) {
        return buildBookListMessage(String.valueOf(chatId), books);
    }

    public SendMessage buildBookListMessage(String chatId, List<Book> books) {
        StringBuilder bookListText = new StringBuilder("Список книг:\n");
        for (Book book : books) {
            bookListText.append(book.getTitle())
                    .append(" - ")
                    .append(book.getAuthor())
                    .append(" - ")
                    .append(book.getStatus().getDisplayNameRu())
                    .append("\n");
        }

        InlineKeyboardButton addButton = new InlineKeyboardButton("Добавить книгу");
        addButton.setCallbackData("add_book");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(addButton)));

        return SendMessage.builder()
                .chatId(chatId)
                .text(bookListText.toString())
                .replyMarkup(markup)
                .build();
    }

    public SendMessage buildBookAddedMessage(Long chatId, String title) {
        return buildBookAddedMessage(String.valueOf(chatId), title);
    }

    public SendMessage buildBookAddedMessage(String chatId, String title) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("Книга \"" + title + "\" добавлена 📚")
                .build();
    }

    public SendMessage buildBookNotFoundByIndexMessage(Long chatId) {
        return buildBookNotFoundByIndexMessage(String.valueOf(chatId));
    }

    public SendMessage buildBookNotFoundByIndexMessage(String chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("Не удалось найти книгу по выбранному индексу.")
                .build();
    }
}
