package com.telegram_bots.bookbot.model.session;

import com.telegram_bots.bookbot.model.dto.LitresBookDto;
import com.telegram_bots.bookbot.model.entities.enums.BookStatus;
import com.telegram_bots.bookbot.model.session.enums.UserState;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserSession {

    private UserState state = UserState.NONE;

    private List<LitresBookDto> searchResults = new ArrayList<>();

    private int currentPage = 0;

    private BookStatus bookStatusFilter = null;

    private Long bookIdForChange;
}
