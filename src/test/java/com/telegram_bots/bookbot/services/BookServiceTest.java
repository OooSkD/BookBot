package com.telegram_bots.bookbot.services;

import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.model.entities.User;
import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import com.telegram_bots.bookbot.repository.BookRepository;
import com.telegram_bots.bookbot.repository.UserRepository;
import com.telegram_bots.bookbot.service.BookService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void testGetAllBooks() {
        List<Book> books = List.of(new Book(), new Book());
        Mockito.when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = bookService.getAllBooks();

        Assertions.assertEquals(2, result.size());
    }

    @Test
    void testGetAllBooksOfUser_WhenUserExists() {
        Long telegramId = 123L;
        User user = new User();
        Mockito.when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(user));
        List<Book> books = List.of(new Book(), new Book());
        Mockito.when(bookRepository.findByUser(user)).thenReturn(books);

        List<Book> result = bookService.getAllBooksOfUser(telegramId);

        Assertions.assertEquals(2, result.size());
    }

    @Test
    void testGetAllBooksOfUser_WhenUserDoesNotExist() {
        Mockito.when(userRepository.findByTelegramId(123L)).thenReturn(Optional.empty());

        List<Book> result = bookService.getAllBooksOfUser(123L);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testGetBookById() {
        Book book = new Book();
        book.setId(1L);
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.getBookById(1L);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(1L, result.get().getId());
    }

    @Test
    void testAddBook_Object() {
        Book book = new Book();
        Mockito.when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.addBook(book);

        Assertions.assertEquals(book, result);
    }

    @Test
    void testAddBook_NewUserAndBook() {
        Long telegramId = 1L;
        String title = "Test";
        String author = "Author";

        Mockito.when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());

        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Mockito.when(bookRepository.save(Mockito.any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.addBook(telegramId, title, author);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(title, result.getTitle());
        Assertions.assertEquals(author, result.getAuthor());
        Assertions.assertEquals(BookStatus.PLANNED, result.getStatus());
    }

    @Test
    void testAddBook_ExistingBook() {
        Long telegramId = 1L;
        String title = "Test";
        String author = "Author";
        User user = new User();
        Book book = new Book();

        Mockito.when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(user));
        Mockito.when(bookRepository.findByTitleAndUser(title, user)).thenReturn(Optional.of(book));

        Book result = bookService.addBook(telegramId, title, author);

        Assertions.assertNull(result);
    }

    @Test
    void testUpdateBook() {
        Long id = 1L;
        Book existingBook = new Book();
        existingBook.setId(id);

        Book newBook = new Book();
        newBook.setTitle("New");
        newBook.setAuthor("New Author");
        newBook.setStatus(BookStatus.READING);
        newBook.setCurrentPage(10);
        newBook.setRating(5);

        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.of(existingBook));
        Mockito.when(bookRepository.save(Mockito.any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.updateBook(id, newBook);

        Assertions.assertEquals("New", result.getTitle());
        Assertions.assertEquals(10, result.getCurrentPage());
    }

    @Test
    void testUpdateBook_NotFound() {
        Mockito.when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> bookService.updateBook(99L, new Book()));

        Assertions.assertEquals("Book not found with id 99", exception.getMessage());
    }

    @Test
    void testDeleteBook() {
        bookService.deleteBook(1L);
        Mockito.verify(bookRepository).deleteById(1L);
    }
}

