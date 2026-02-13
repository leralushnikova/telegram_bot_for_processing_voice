package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.bot.SpeechRecognitionBot;
import com.telegram_bot_for_processing_voice.service.TelegramFileDownloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Сервис загрузки аудио сообщений.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramFileDownloaderImpl implements TelegramFileDownloader {

    private static final int CONNECT_TIMEOUT_MS = 30_000;
    private static final int READ_TIMEOUT_MS = 60_000;

    @Override
    public InputStream downloadFileAsStream(String fileId, SpeechRecognitionBot bot)
            throws TelegramApiException, IOException {
        
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);
        org.telegram.telegrambots.meta.api.objects.File file = bot.execute(getFile);

        String fileUrl = file.getFileUrl(bot.getBotToken());
        log.info("Скачиваю файл: {}, fileId: {}", fileUrl, fileId);

        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Ошибка скачивания файла. HTTP код: " + responseCode);
        }

        return connection.getInputStream();
    }
}