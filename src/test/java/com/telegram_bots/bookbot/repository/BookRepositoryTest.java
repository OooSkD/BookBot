package com.telegram_bots.bookbot.repository;

import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.model.entities.User;
import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private User createAndSaveUser() {
        User user = new User();
        user.setTelegramId(123L);
        user.setUsername("reader");
        user.setFirstName("Book");
        user.setLastName("Lover");
        user.setModifiedAt(Timestamp.from(Instant.now()));
        return userRepository.save(user);
    }

    private Book createBook(String title, User user) {
        return Book.builder()
                .title(title)
                .author("Author Name")
                .status(BookStatus.READING)
                .addedDate(LocalDate.now())
                .startDate(LocalDate.now())
                .finishDate(null)
                .rating(null)
                .currentPage(10)
                .modifiedAt(Timestamp.from(Instant.now()))
                .user(user)
                .build();
    }

    @BeforeEach
    void setup() {
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Сохранение и поиск книги по title и user")
    void findByTitleAndUser() {
        User user = createAndSaveUser();
        Book book = createBook("Test Book", user);
        bookRepository.save(book);

        Optional<Book> found = bookRepository.findByTitleAndUser("Test Book", user);

        assertThat(found).isPresent();
        assertThat(found.get().getAuthor()).isEqualTo("Author Name");
    }

    @Test
    @DisplayName("Поиск всех книг")
    void findAllBooks() {
        User user = createAndSaveUser();
        bookRepository.save(createBook("Book 1", user));
        bookRepository.save(createBook("Book 2", user));

        List<Book> books = bookRepository.findAll();

        assertThat(books).hasSize(2);
    }

    @Test
    @DisplayName("Поиск книг по пользователю")
    void findByUser() {
        User user1 = createAndSaveUser();
        User user2 = userRepository.save(User.builder()
                .telegramId(456L)
                .username("otheruser")
                .firstName("Other")
                .lastName("User")
                .modifiedAt(Timestamp.from(Instant.now()))
                .build());

        bookRepository.save(createBook("Book A", user1));
        bookRepository.save(createBook("Book B", user1));
        bookRepository.save(createBook("Book C", user2));

        List<Book> user1Books = bookRepository.findByUser(user1);
        List<Book> user2Books = bookRepository.findByUser(user2);

        assertThat(user1Books).hasSize(2);
        assertThat(user2Books).hasSize(1);
    }
}
