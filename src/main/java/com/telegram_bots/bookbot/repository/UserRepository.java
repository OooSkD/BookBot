package com.telegram_bots.bookbot.repository;

import com.telegram_bots.bookbot.model.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByTelegramId(Long telegramId);
}
