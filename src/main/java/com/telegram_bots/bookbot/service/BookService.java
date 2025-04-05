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

    public Book updateBook(Long id, Book bookDetails) {
        Optional<Book> book = bookRepository.findById(id);
        if (book.isPresent()) {
            Book existingBook = book.get();
            existingBook.setTitle(bookDetails.getTitle());
            existingBook.setAuthor(bookDetails.getAuthor());
            existingBook.setStatus(bookDetails.getStatus());
            return bookRepository.save(existingBook);
        }
        return null;  // или выбрасывать исключение, если книга не найдена
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}
