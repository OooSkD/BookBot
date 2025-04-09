package com.telegram_bots.bookbot.bot;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.service.BookService;
import com.telegram_bots.bookbot.service.UserStateService;
import com.telegram_bots.bookbot.service.LitresService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

//TODO разбить на классы и методы разбить
@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private final BookService bookService;
    private final UserStateService userStateService;
    private final LitresService litresService;

    public MyTelegramBot(BookService bookService,
                         UserStateService userStateService,
                         LitresService litresService) {
        this.bookService = bookService;
        this.userStateService = userStateService;
        this.litresService = litresService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void sendTextMessage(Long userId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(userId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendFullMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendBookList(Long userId) {
        List<Book> books = bookService.getAllBooks();
        StringBuilder bookListText = new StringBuilder("Список книг:\n");

        for (Book book : books) {
            bookListText.append(book.getTitle()).append("\n");
        }

        SendMessage message = new SendMessage();
        message.setChatId(userId.toString());
        message.setText(bookListText.toString());

        // Создание кнопки
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton("Добавить книгу");
        button.setCallbackData("add_book");
        row.add(button);
        markup.setKeyboard(List.of(row));

        message.setReplyMarkup(markup);
        sendFullMessage(message);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Long userId = update.getMessage().getFrom().getId();

            if (update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();

                if (messageText.equals("/start") || messageText.toLowerCase().contains("привет")) {
                    sendWelcomeMessage(userId);
                    return;
                }

                // Если мы ожидаем от пользователя название книги
                if (userStateService.isWaitingForBookTitle(userId)) {
                    String bookTitle = messageText;
                    // Проверка книги через litres
                    sendSearchResults(bookTitle, userId);
                    return;
                }
            }
        }

        // Обработка нажатий на кнопки
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long userId = update.getCallbackQuery().getFrom().getId();

            switch (callbackData) {
                case "add_book":
                    userStateService.setWaitingForBookTitle(userId, true); // Запоминаем, что ждём название
                    requestBookTitle(userId);
                    break;
                case "show_books":
                    sendBookList(userId);
                    break;
                case "cancel_add_book":
                    userStateService.setWaitingForBookTitle(userId, false);
                    sendTextMessage(userId, "Добавление книги отменено.");
                    break;
                case "select_book":
                    String data = callbackData.substring("select_book:".length());
                    String[] parts = data.split("\\|");
                    String title = parts[0];
                    String author = parts.length > 1 ? parts[1] : "";

                    // сохраняем в базу
                    bookService.addBook(userId, title, author);
                    sendTextMessage(userId, "Книга \"" + title + "\" добавлена 📚");
                    sendWelcomeMessage(userId);
                    break;
            }
        }
    }

    private void sendWelcomeMessage(Long userId) {
        SendMessage message = new SendMessage();
        message.setChatId(userId.toString());
        message.setText("Привет! Я ваш книжный бот. Чем могу помочь?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton addBookButton = new InlineKeyboardButton("Добавить книгу");
        addBookButton.setCallbackData("add_book");

        InlineKeyboardButton showBooksButton = new InlineKeyboardButton("Мои книги");
        showBooksButton.setCallbackData("show_books");

        row.add(addBookButton);
        row.add(showBooksButton);
        markup.setKeyboard(List.of(row));

        message.setReplyMarkup(markup);

        sendFullMessage(message);
    }

    private void requestBookTitle(Long userId) {
        SendMessage message = new SendMessage();
        message.setChatId(userId.toString());
        message.setText("Пожалуйста, введите название книги:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton cancelButton = new InlineKeyboardButton("❌ Отмена");
        cancelButton.setCallbackData("cancel_add_book");
        row.add(cancelButton);
        markup.setKeyboard(List.of(row));

        message.setReplyMarkup(markup);

        sendFullMessage(message);
    }

    private void sendSearchResults(String bookTitle, Long chatId) {
        List<LitresBookDto> foundBooks = litresService.searchBooks(bookTitle);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Вот что удалось найти:");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (LitresBookDto book : foundBooks) {
            String bookText = "📖 " + book.getTitle() + "\n✍️ " + book.getAuthor();
            SendMessage bookMessage = new SendMessage(chatId.toString(), bookText);

            InlineKeyboardButton selectButton = new InlineKeyboardButton();
            selectButton.setText("✅ Выбрать");
            selectButton.setCallbackData("select_book:" + book.getTitle() + "|" + book.getAuthor());

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(selectButton);

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            markup.setKeyboard(List.of(row));

            bookMessage.setReplyMarkup(markup);
            sendFullMessage(bookMessage);
        }

        // Кнопка "Отмена" внизу
        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("❌ Отмена");
        cancelButton.setCallbackData("cancel_add_book");

        InlineKeyboardMarkup cancelMarkup = new InlineKeyboardMarkup();
        cancelMarkup.setKeyboard(List.of(List.of(cancelButton)));

        SendMessage cancelMessage = new SendMessage(chatId.toString(), " ");
        cancelMessage.setReplyMarkup(cancelMarkup);
        sendFullMessage(cancelMessage);
    }
}
