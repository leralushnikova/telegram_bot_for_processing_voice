package com.telegram_bot_for_processing_voice.bot;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.telegram_bot_for_processing_voice.util.Constants.ERROR_MESSAGE;
import static com.telegram_bot_for_processing_voice.util.Constants.MAX_LENGTH_MESSAGE;

/**
 * Сервис для отправки уведомлений администратору бота.
 */
@Slf4j
@Component
public class AdminNotifier {

    @Value("${telegram.notifier.token}")
    private String notifierToken;

    @Value("${telegram.notifier.chat-id}")
    private Long adminChatId;

    private NotifierBot notifierBot;

    /**
     * Инициализирует бота для отправки уведомлений после создания бина.
     */
    @PostConstruct
    public void init() {
        DefaultBotOptions options = new DefaultBotOptions();
        this.notifierBot = new NotifierBot(options, notifierToken);
        log.info("AdminNotifier инициализирован для чата {}", adminChatId);
    }

    /**
     * Отправляет уведомление об ошибке администратору.
     *
     * @param errorMessage краткое описание ошибки
     * @param e исключение, содержащее детали и stack trace
     * @param userChatId ID пользователя, у которого произошла ошибка (может быть null)
     */
    public void notifyError(String errorMessage, Exception e, Long userChatId) {
        try {
            String formattedMessage = formatErrorMessage(errorMessage, e, userChatId);
            sendToAdmin(formattedMessage);
        } catch (Exception ex) {
            log.error("Не удалось отправить уведомление админу: {}", ex.getMessage());
        }
    }

    /**
     * Форматирует сообщение об ошибке по заданному шаблону.
     *
     * @param errorMessage краткое описание ошибки
     * @param e исключение с деталями
     * @param userChatId ID пользователя
     * @return отформатированное сообщение в Markdown
     */
    private String formatErrorMessage(String errorMessage, Exception e, Long userChatId) {
        return String.format(ERROR_MESSAGE,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                userChatId != null ? userChatId : "неизвестно",
                errorMessage,
                e.getMessage() != null ? e.getMessage() : "нет описания",
                truncateStackTrace(getStackTraceAsString(e))
        );
    }

    /**
     * Отправляет сообщение администратору через Telegram бота.
     *
     * @param message текст сообщения
     */
    private void sendToAdmin(String message) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(adminChatId.toString());
            sendMessage.setText(message);
            sendMessage.setParseMode("Markdown");

            notifierBot.execute(sendMessage);
            log.debug("Уведомление отправлено админу");
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение админу: {}", e.getMessage());
        }
    }

    /**
     * Преобразует stack trace исключения в строку.
     *
     * @param e исключение
     * @return строковое представление stack trace
     */
    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Обрезает stack trace до допустимой длины.
     *
     * @param stackTrace полный stack trace
     * @return обрезанный stack trace
     */
    private String truncateStackTrace(String stackTrace) {
        if (stackTrace.length() <= MAX_LENGTH_MESSAGE) {
            return stackTrace;
        }
        return stackTrace.substring(0, MAX_LENGTH_MESSAGE) + "...";
    }

    /**
     * Внутренний класс бота для уведомлений.
     */
    private static class NotifierBot extends DefaultAbsSender {

        protected NotifierBot(DefaultBotOptions options, String botToken) {
            super(options, botToken);
        }
    }
}