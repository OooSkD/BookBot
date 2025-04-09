package com.telegram_bots.bookbot.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    private Long telegramId;
    private String username;
    private String firstName;
    private String lastName;

    @Column(name = "modified_at")
    private Timestamp modifiedAt;
}
