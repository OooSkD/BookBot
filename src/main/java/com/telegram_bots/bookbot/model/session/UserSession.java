package com.telegram_bots.bookbot.model.session;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserSession {

    private boolean waitingForBookTitle;

    private boolean waitingForPageInput;

    private boolean waitingForRatingInput;

    private List<LitresBookDto> searchResults = new ArrayList<>();

    private int currentPage = 0;

    private BookStatus bookStatusFilter = null;

    private Long bookIdForPageInput;

    private Long bookIdForRatingInput;
}
