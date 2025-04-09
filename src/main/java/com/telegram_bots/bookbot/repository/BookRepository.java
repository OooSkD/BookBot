package com.telegram_bots.bookbot.repository;

import com.telegram_bots.bookbot.model.entities.Book;
import com.telegram_bots.bookbot.model.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends CrudRepository<Book, Long> {
    List<Book> findAll();
    Optional<Book> findByTitleAndUser(String title, User user);
    List<Book> findByUser(User user);

}
