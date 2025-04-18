package com.telegram_bots.bookbot.repository;

import com.telegram_bots.bookbot.model.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Поиск пользователя по Telegram ID")
    void findByTelegramId() {
        User user = new User();
        user.setTelegramId(123456L);
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setModifiedAt(Timestamp.from(Instant.now()));

        userRepository.save(user);

        Optional<User> found = userRepository.findByTelegramId(123456L);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }
}
