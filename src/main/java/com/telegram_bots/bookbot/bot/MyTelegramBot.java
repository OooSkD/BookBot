package com.telegram_bots.bookbot.bot;

import com.telegram_bots.bookbot.bot.service.BotResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;


@Slf4j
@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final BotResponseService botResponseService;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.username}")
    private String botUsername;

    public MyTelegramBot(BotResponseService botResponseService) {
        this.botResponseService = botResponseService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        List<SendMessage> messagesToSend = null;
        DeleteMessage messageToDelete = null;
        if (update.hasMessage() && update.getMessage().hasText()) {
            messagesToSend = botResponseService.handleTextMessage(update);
        } else if (update.hasCallbackQuery()) {
            messagesToSend = botResponseService.handleCallbackQuery(update);
            messageToDelete = botResponseService.handleDeleteMessage(update);
        }

        sendMessages(messagesToSend);
        deleteMessage(messageToDelete);
    }

    private void sendMessages(List<SendMessage> messages) {
        if (messages != null && !messages.isEmpty()) {
            try {
                for (SendMessage msg : messages) {
                    execute(msg);
                }
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке сообщения: ", e);
            }
        }
    }

    private void deleteMessage(DeleteMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Ошибка при удалении сообщения: ", e);
            }
        }
    }
}

