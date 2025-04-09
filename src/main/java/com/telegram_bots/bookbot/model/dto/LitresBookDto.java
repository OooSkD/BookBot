package com.telegram_bots.bookbot.model.dto;

import lombok.Data;

@Data
public class LitresBookDto {
    private String title;
    private String author;

    public LitresBookDto(String title, String author) {
        this.title = title;
        this.author = author;
    }
}
