package com.telegram_bots.bookbot.model.entities.enums;

public enum BookStatus {
    PLANNED("Запланировано"),
    READING("Читаю"),
    READ("Прочитана"),
    READING_AGAIN("Перечитываю"),
    READ_AGAIN("Перечитана"),
    DROPPED("Брошено"),
    ON_HOLD("Отложено");

    private final String displayNameRu;

    BookStatus(String displayNameRu) {
        this.displayNameRu = displayNameRu;
    }

    public String getDisplayNameRu() {
        return displayNameRu;
    }
}
