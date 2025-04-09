package com.telegram_bots.bookbot.repository;

import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.model.entities.User;
import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveBookWithUser() {
        User user = new User();
        user.setTelegramId(999L);
        user.setUsername("reader");
        entityManager.persist(user);

        Book book = new Book();
        book.setTitle("Мастер и Маргарита");
        book.setAuthor("Булгаков");
        book.setStatus(BookStatus.READING);
        book.setUser(user);
        book.setRating(9);
        book.setCurrentPage(100);

        entityManager.persistAndFlush(book);

        Optional<Book> found = bookRepository.findById(book.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getTitle()).isEqualTo("Мастер и Маргарита");
        assertThat(found.get().getAuthor()).isEqualTo("Булгаков");
        assertThat(found.get().getStatus()).isEqualTo(BookStatus.READING);

        // Проверяем, что пользователь книги совпадает с сохранённым пользователем
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());  // Здесь проверяем по id пользователя
        assertThat(found.get().getRating()).isEqualTo(9);
        assertThat(found.get().getCurrentPage()).isEqualTo(100);
    }
}
