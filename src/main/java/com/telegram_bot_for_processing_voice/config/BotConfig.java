package com.telegram_bot_for_processing_voice.config;

import com.telegram_bot_for_processing_voice.bot.SpeechRecognitionBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Конфигурационный класс для настройки и регистрации Telegram бота.
 */
@Slf4j
@Configuration
@Profile("!test")
public class BotConfig {

    /**
     * Создает экземпляр TelegramBotsApi для управления сессиями ботов.
     *
     * @param bot - Telegram бот для распознавания речи
     * @return экземпляр TelegramBotsApi
     */
    @Bean
    public TelegramBotsApi telegramBotsApi(SpeechRecognitionBot bot) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            log.info("Telegram бот успешно зарегистрирован!");
            return botsApi;
        } catch (TelegramApiException e) {
            log.error("Ошибка регистрации бота", e);
            throw new RuntimeException("Не удалось зарегистрировать бота", e);
        }
    }
}