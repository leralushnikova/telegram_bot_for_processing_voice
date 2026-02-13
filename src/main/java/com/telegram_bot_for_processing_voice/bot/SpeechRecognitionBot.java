package com.telegram_bot_for_processing_voice.bot;

import com.telegram_bot_for_processing_voice.handler.MessageHandler;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static com.telegram_bot_for_processing_voice.util.Constants.TEXT_MESSAGE;

/**
 * Telegram бот для распознавания речи и анализа спортивной статистики.
 */
@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class SpeechRecognitionBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${yandex.storage.bucket}")
    private String bucket;

    private final List<MessageHandler> handlers;
    private final MessageSender messageSender;

    /**
     * Инициализирует бота после создания бина.
     */
    @PostConstruct
    public void init() {
        messageSender.registerBotCommands(this);
    }

    /**
     * Обрабатывает входящие обновления от Telegram.
     *
     * @param update объект обновления от Telegram API
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();
        Long chatId = message.getChatId();

        try {

            boolean messageHandled = handlers.stream()
                    .filter(h -> h.canHandle(message))
                    .findFirst()
                    .map(h -> {
                        h.handle(message, this);
                        return true;
                    })
                    .orElse(false);

            if (!messageHandled) {
                messageSender.sendText(this, chatId, TEXT_MESSAGE);
            }

        } catch (Exception e) {
            log.error("Ошибка обработки сообщения", e);
            messageSender.sendError(this, chatId, "❌ Ошибка: " + e.getMessage());
        }
    }
}