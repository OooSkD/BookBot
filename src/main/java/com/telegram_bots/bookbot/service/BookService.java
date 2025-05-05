package com.telegram_bots.bookbot.service;

import com.telegram_bots.bookbot.model.dto.Statistics;
import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.model.entities.User;
import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import com.telegram_bots.bookbot.repository.BookRepository;
import com.telegram_bots.bookbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

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

    public List<Book> getAllBooksOfUser(Long telegramId) {
        Optional<User> optionalUser = userRepository.findByTelegramId(telegramId);

        if (optionalUser.isPresent()) {
            return bookRepository.findByUser(optionalUser.get());
        }
        return new ArrayList<>();
    }

    public Optional<Book> getBookOptionalById(Long id) {
        return bookRepository.findById(id);
    }

    public Book getBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id " + bookId));
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
        book.setStatus(BookStatus.PLANNED);
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

    public Book updatePage(Book book, int newPage) {
        book.setCurrentPage(newPage);
        return bookRepository.save(book);
    }

    public Book updateRating(Book book, int rating) {
        book.setRating(rating);
        return bookRepository.save(book);
    }

    public Book updateStatus(Book book, BookStatus status) {
        book.setStatus(status);
        if (status == BookStatus.READING && book.getStartDate() == null) {
            book.setStartDate(LocalDate.now());
        } else if (status == BookStatus.READ && book.getFinishDate() == null) {
            book.setFinishDate(LocalDate.now());
        }
        return bookRepository.save(book);
    }

    public Statistics getStatisticsForUser(Long userId) {
        List<Book> books = getAllBooksOfUser(userId);

        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.now();
        int thisYear = today.getYear();

        int todayBooks = 0;
        int todayPages = 0;
        int monthBooks = 0;
        int monthPages = 0;
        int yearBooks = 0;
        int yearPages = 0;

        Book biggestBook = null;

        for (Book book : books) {
            LocalDate finished = book.getFinishDate();
            if (finished == null) {
                continue;
            }
            int pages = book.getCurrentPage();

            if (finished.isEqual(today)) {
                todayBooks++;
                todayPages += pages;
            }
            if (YearMonth.from(finished).equals(thisMonth)) {
                monthBooks++;
                monthPages += pages;
            }
            if (finished.getYear() == thisYear) {
                yearBooks++;
                yearPages += pages;
            }

            if (biggestBook == null || pages > biggestBook.getCurrentPage()) {
                biggestBook = book;
            }
        }

        return new Statistics(
                todayBooks, todayPages,
                monthBooks, monthPages,
                yearBooks, yearPages,
                biggestBook != null ? biggestBook.getTitle() : "-",
                biggestBook != null ? biggestBook.getCurrentPage() : 0
        );
    }
}
