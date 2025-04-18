package com.telegram_bots.bookbot.bot.service;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import com.telegram_bots.bookbot.utils.WelcomeMessageProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class MessageServiceTest {


    private final MessageService messageService = new MessageService(new WelcomeMessageProvider() {
        @Override
        public String getRandomMessage() {
            return "Привет!";
        }
    });

    @Nested
    class WelcomeTests {
        @Test
        void buildWelcomeMessage_ShouldContainExpectedTextAndButtons() {
            SendMessage message = messageService.buildWelcomeMessage("123");

            assertThat(message.getText()).isEqualTo("Привет!");
            assertThat(message.getChatId()).isEqualTo("123");
            InlineKeyboardMarkup markup = (InlineKeyboardMarkup) message.getReplyMarkup();
            assertThat(markup.getKeyboard()).hasSize(1);
            assertThat(markup.getKeyboard().get(0)).hasSize(2);
        }
    }

    @Nested
    class RequestTitleTests {
        @Test
        void buildRequestBookTitleMessage_ShouldReturnCorrectText() {
            SendMessage message = messageService.buildRequestBookTitleMessage("123");
            assertThat(message.getText()).contains("Введите название книги");
        }
    }

    @Nested
    class BookSearchResultsTests {
        @Test
        void buildBookSearchResults_ShouldReturnButtonsForEachBook() {
            List<LitresBookDto> books = List.of(
                    new LitresBookDto("Мастер и Маргарита", "Булгаков"),
                    new LitresBookDto("Преступление и наказание", "Достоевский")
            );

            SendMessage message = messageService.buildBookSearchResults("123", books);

            InlineKeyboardMarkup markup = (InlineKeyboardMarkup) message.getReplyMarkup();
            assertThat(markup.getKeyboard()).hasSize(3); // 2 книги + кнопка Отмена
            assertThat(markup.getKeyboard().get(0).get(0).getText()).contains("Мастер и Маргарита");
            assertThat(markup.getKeyboard().get(2).get(0).getText()).contains("Отмена");
        }

        @Test
        void buildNoBooksFoundMessage_ShouldContainSadTextAndCancelButton() {
            SendMessage message = messageService.buildNoBooksFoundMessage("123");

            assertThat(message.getText()).contains("Ничего не нашлось");
            InlineKeyboardMarkup markup = (InlineKeyboardMarkup) message.getReplyMarkup();
            assertThat(markup.getKeyboard().get(0).get(0).getText()).contains("Отмена");
        }
    }

    @Nested
    class CancelTests {
        @Test
        void buildCancelledMessage_ShouldContainCuteTextAndTwoButtons() {
            SendMessage message = messageService.buildCancelledMessage("123");

            assertThat(message.getText()).contains("книжка подождёт");
            InlineKeyboardMarkup markup = (InlineKeyboardMarkup) message.getReplyMarkup();
            assertThat(markup.getKeyboard().get(0)).hasSize(2);
        }
    }

    @Nested
    class UnknownCommandTests {
        @Test
        void buildUnknownCommandMessage_ShouldReturnExpectedText() {
            SendMessage message = messageService.buildUnknownCommandMessage("123");
            assertThat(message.getText()).contains("Неизвестная команда");
        }

        @Test
        void buildUnknownCallbackMessage_shouldReturnExpectedMessage() {
            String chatId = "123";

            SendMessage result = messageService.buildUnknownCallbackMessage(chatId);

            assertEquals(chatId, result.getChatId());
            assertEquals("Неизвестное действие. Попробуйте снова.", result.getText());
        }
    }

    @Nested
    class BookAddedTests {
        @Test
        void buildBookAddedMessage_ShouldContainBookTitle() {
            SendMessage message = messageService.buildBookAddedMessage("123", "Тестовая книга");
            assertThat(message.getText()).contains("Тестовая книга");
        }

        @Test
        void buildBookNotFoundByIndexMessage_shouldReturnExpectedMessage() {
            String chatId = "123";

            SendMessage result = messageService.buildBookNotFoundByIndexMessage(chatId);

            assertEquals(chatId, result.getChatId());
            assertEquals("Не удалось найти книгу по выбранному индексу.", result.getText());
        }
    }

    @Nested
    class BooksTextTests {
        // buildBooksText
        @Test
        void buildBooksText_ShouldHandleEmptyList() {
            String text = messageService.buildBooksText(List.of(), null);
            assertThat(text).contains("Нет книг");
        }

        @Test
        void buildBooksText_shouldReturnEmptyMessage_whenBookListIsEmpty() {
            List<Book> books = new ArrayList<>();

            String result = messageService.buildBooksText(books, null);

            assertTrue(result.contains("Нет книг для отображения."));
        }

        @Test
        void buildBooksText_shouldReturnBookList_whenBooksArePresent() {
            List<Book> books = List.of(
                    Book.builder()
                            .id(1L)
                            .title("Книга 1")
                            .author("Автор 1")
                            .status(BookStatus.PLANNED)
                            .build(),
                    Book.builder()
                            .id(2L)
                            .title("Книга 2")
                            .author("Автор 2")
                            .status(BookStatus.READ)
                            .build()
            );

            String result = messageService.buildBooksText(books, null);

            assertTrue(result.contains("Книга 1 - Автор 1 - Запланировано"));
            assertTrue(result.contains("Книга 2 - Автор 2 - Прочитана"));
        }
    }

    @Nested
    class BookButtonTests {

        @Test
        void buildBookButtons_ShouldCreateButtonsForEachBook() {
            Book book1 = Book.builder()
                    .id(1L)
                    .title("Книга 1")
                    .author("Автор")
                    .status(BookStatus.PLANNED)
                    .build();
            Book book2 = Book.builder()
                    .id(1L)
                    .title("Книга 2")
                    .author("Автор")
                    .status(BookStatus.PLANNED)
                    .build();
            List<Book> books = List.of(book1, book2);

            List<List<InlineKeyboardButton>> buttons = messageService.buildBookButtons(books);

            assertThat(buttons).hasSize(2);
            assertThat(buttons.get(0).get(0).getText()).isEqualTo("Книга 1");
            assertThat(buttons.get(0).get(0).getCallbackData()).isEqualTo("manage_book:1");
        }

        @Test
        void buildPaginationButtons_ShouldShowPrevAndNext() {
            List<InlineKeyboardButton> buttons = messageService.buildPaginationButtons(1, 30, 10);
            assertThat(buttons).hasSize(2);
            assertThat(buttons.get(0).getText()).contains("Назад");
            assertThat(buttons.get(1).getText()).contains("Вперёд");
        }

        @Test
        void buildPaginationButtons_ShouldShowOnlyNextIfOnFirstPage() {
            List<InlineKeyboardButton> buttons = messageService.buildPaginationButtons(0, 30, 10);
            assertThat(buttons).hasSize(1);
            assertThat(buttons.get(0).getText()).contains("Вперёд");
        }

        @Test
        void buildPaginationButtons_ShouldShowOnlyPrevIfOnLastPage() {
            List<InlineKeyboardButton> buttons = messageService.buildPaginationButtons(2, 25, 10);
            assertThat(buttons).hasSize(1);
            assertThat(buttons.get(0).getText()).contains("Назад");
        }

        @Test
        void buildFilterAndAddButtons_ShouldReturnTwoRows() {
            List<List<InlineKeyboardButton>> rows = messageService.buildFilterAndAddButtons();
            assertThat(rows).hasSize(2);
            assertThat(rows.get(0).get(0).getText()).contains("Изменить фильтр");
            assertThat(rows.get(1).get(0).getText()).contains("Добавить книгу");
        }
    }
}
