package com.telegram_bots.bookbot.model.dto;

import lombok.Data;

@Data
public class LitresBookDto {
    private String title;
    private String author;
    private Integer totalPages;

    public LitresBookDto(String title, String author, Integer total_pages) {
        this.title = title;
        this.author = author;
        this.totalPages = total_pages;
    }

    public LitresBookDto(String title, String author) {
        this.title = title;
        this.author = author;
    }
}
