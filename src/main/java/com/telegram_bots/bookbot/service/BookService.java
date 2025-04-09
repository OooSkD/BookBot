package com.telegram_bots.bookbot.service;

import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.model.entities.User;
import com.telegram_bots.bookbot.repository.BookRepository;
import com.telegram_bots.bookbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookService(UserRepository userRepository, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    public Book addBook(Long telegramId, String title, String author) {
        // 1. Получаем пользователя по Telegram ID
        Optional<User> optionalUser = userRepository.findByTelegramId(telegramId);
        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            Optional<Book> existingBook = bookRepository.findByTitleAndUser(title, user);
            if (existingBook.isPresent()) {
                return null; // книга уже есть — ничего не сохраняем
            }
        } else {
            // 2. Создаем нового пользователя
            user = new User();
            user.setTelegramId(telegramId);
            userRepository.save(user);
        }

        // 4. Создаем и сохраняем новую книгу
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setUser(user);
        book.setAddedDate(LocalDate.now());

        return bookRepository.save(book);
    }

    public Book updateBook(Long id, Book book) {
        // Проверяем, существует ли книга с таким id
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id " + id));

        // Обновляем поля книги
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setStatus(book.getStatus());
        existingBook.setUser(book.getUser());
        existingBook.setRating(book.getRating());
        existingBook.setCurrentPage(book.getCurrentPage());

        // Сохраняем обновлённую книгу в базе данных
        return bookRepository.save(existingBook);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}
