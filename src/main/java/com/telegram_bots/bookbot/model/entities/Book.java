package com.telegram_bots.bookbot.model.entities;

import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // Название книги
    private String author; // Автор книги

    @Enumerated(EnumType.STRING)
    private BookStatus status; // Статус книги (например, Читаю, Прочитана и т.д.)

    private LocalDate addedDate; // Дата добавления книги в список
    private LocalDate startDate; // Дата начала чтения
    private LocalDate finishDate; // Дата окончания чтения
    private Integer rating; // Оценка книги от пользователя

    @Column(name = "total_pages")
    private Integer totalPages;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // пользователь, который добавил эту книгу

    private Integer currentPage; // Номер страницы, на которой остановился пользователь

    @Column(name = "modified_at")
    @UpdateTimestamp
    private Timestamp modifiedAt; // Дата, когда информация по книге была обновлена

    public Book(String title, String author, BookStatus status) {
        this.title = title;
        this.author = author;
        this.status = status;
    }
}
