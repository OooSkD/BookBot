package com.telegram_bots.bookbot.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
