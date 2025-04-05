package com.telegram_bots.bookbot.entities;

import com.telegram_bots.bookbot.entities.enums.BookStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // Название книги
    private String author; // Автор книги
    private Integer pageNumber; // Номер страницы, на которой остановился пользователь
    private LocalDate updatedDate; // Дата, когда информация по книге была обновлена
    private Integer rating; // Оценка книги от пользователя
    private LocalDate addedToWishlistDate; // Дата, когда книга была добавлена в список желаемого
    private String userId; // userId, чтобы идентифицировать пользователя

    //TODO: добавить поле в котором будет отмечено о начале чтения книги
    //TODO: добавить отметку об окончании чтения

    @Enumerated(EnumType.STRING)
    private BookStatus status; // Статус книги (например, Читаю, Прочитана и т.д.)

}
