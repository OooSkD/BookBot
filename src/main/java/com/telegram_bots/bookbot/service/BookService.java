package com.telegram_bots.bookbot.service;

import com.telegram_bots.bookbot.entities.Book;
import com.telegram_bots.bookbot.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Book addBook(Book book) {
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
