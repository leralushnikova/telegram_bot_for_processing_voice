package com.telegram_bot_for_processing_voice.service;

import com.telegram_bot_for_processing_voice.bot.SpeechRecognitionBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Интерфейс загрузки аудио сообщений.
 */
public interface TelegramFileDownloader {

    /**
     * Преобразует аудио сообщение в поток данных.
     *
     * @param fileId идентификатор файла
     * @param bot экземпляр бота SpeechRecognitionBot
     * @return поток данных.
     * @throws IOException если произошла ошибка при скачивании файла
     * @throws TelegramApiException если произошла ошибка при скачивании файла
     */
    InputStream downloadFileAsStream(String fileId, SpeechRecognitionBot bot) throws TelegramApiException, IOException;
}