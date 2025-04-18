package com.telegram_bots.bookbot.bot.service;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import com.telegram_bots.bookbot.utils.ButtonUtils;
import com.telegram_bots.bookbot.utils.WelcomeMessageProvider;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import com.telegram_bots.bookbot.model.entities.Book;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final WelcomeMessageProvider welcomeMessageProvider;

    public MessageService(WelcomeMessageProvider welcomeMessageProvider) {
        this.welcomeMessageProvider = welcomeMessageProvider;
    }

    public SendMessage buildWelcomeMessage(Long chatId) {
        return buildWelcomeMessage(String.valueOf(chatId));
    }

    public SendMessage buildWelcomeMessage(String chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(ButtonUtils.createButton("Посмотреть мои книги", "show_books"));
        row.add(ButtonUtils.createButton("Добавить книгу", "add_book"));
        markup.setKeyboard(List.of(row));

        return SendMessage.builder()
                .chatId(chatId)
                .text(welcomeMessageProvider.getRandomMessage())
                .replyMarkup(markup)
                .build();
    }


    public SendMessage buildRequestBookTitleMessage(Long chatId) {
        return buildRequestBookTitleMessage(String.valueOf(chatId));
    }

    public SendMessage buildRequestBookTitleMessage(String chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("Введите название книги или автора:")
                .build();
    }


    public SendMessage buildBookSearchResults(Long chatId, List<LitresBookDto> books) {
        return buildBookSearchResults(String.valueOf(chatId), books);
    }

    public SendMessage buildBookSearchResults(String chatId, List<LitresBookDto> books) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (int i = 0; i < books.size(); i++) {
            LitresBookDto book = books.get(i);
            String text = book.getTitle() + " - " + book.getAuthor();
            String callback = "select_book:" + i;
            rows.add(List.of(ButtonUtils.createButton(text, callback)));
        }

        rows.add(List.of(ButtonUtils.createButton("❌ Отмена", "cancel_added_book")));

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(rows).build();

        return SendMessage.builder()
                .chatId(chatId)
                .text("Выберите книгу:")
                .replyMarkup(markup)
                .build();
    }

    public SendMessage buildNoBooksFoundMessage(Long chatId) {
        return buildNoBooksFoundMessage(String.valueOf(chatId));
    }

    public SendMessage buildNoBooksFoundMessage(String chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row = List.of(ButtonUtils.createButton("❌ Отмена", "cancel_added_book"));
        markup.setKeyboard(List.of(row));

        return SendMessage.builder()
                .chatId(chatId)
                .text("Ничего не нашлось 😢\nПопробуй ввести название ещё раз, может оно немного другое?")
                .replyMarkup(markup)
                .build();
    }

    public SendMessage buildCancelledMessage(Long chatId) {
        return buildCancelledMessage(String.valueOf(chatId));
    }

    public SendMessage buildCancelledMessage(String chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(ButtonUtils.createButton("Посмотреть мои книги", "show_books"));
        row.add(ButtonUtils.createButton("Добавить книгу", "add_book"));
        markup.setKeyboard(List.of(row));

        return SendMessage.builder()
                .chatId(chatId)
                .text("📖 Ничего страшного, книжка подождёт своего часа ☁️")
                .replyMarkup(markup)
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

    public String buildBooksText(List<Book> books, BookStatus filter) {
        StringBuilder text = new StringBuilder("📚 *Список книг*");
        if (filter != null) {
            text.append(" (").append(filter.getDisplayNameRu()).append(")");
        }
        text.append(":\n\n");

        if (books.isEmpty()) {
            text.append("Нет книг для отображения.");
        } else {
            for (Book book : books) {
                text.append(book.getTitle())
                        .append(" - ")
                        .append(book.getAuthor())
                        .append(" - ")
                        .append(book.getStatus().getDisplayNameRu())
                        .append("\n");
            }
        }

        return text.toString();
    }

    public List<List<InlineKeyboardButton>> buildBookButtons(List<Book> books) {
        return books.stream()
                .map(book ->
                        List.of(ButtonUtils.createButton(book.getTitle(), "manage_book:" + book.getId())))
                .collect(Collectors.toList());
    }

    public List<InlineKeyboardButton> buildPaginationButtons(int currentPage, int totalBooks, int pageSize) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        if (currentPage > 0) {
            buttons.add(ButtonUtils.createButton("⬅️ Назад", "books_prev_page"));
        }
        if ((currentPage + 1) * pageSize < totalBooks) {
            buttons.add(ButtonUtils.createButton("Вперёд ➡️", "books_next_page"));
        }
        return buttons;
    }

    public List<List<InlineKeyboardButton>> buildFilterAndAddButtons() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(ButtonUtils.createButton("📂 Изменить фильтр", "change_filter")));
        rows.add(List.of(ButtonUtils.createButton("➕ Добавить книгу", "add_book")));
        return rows;
    }
}
