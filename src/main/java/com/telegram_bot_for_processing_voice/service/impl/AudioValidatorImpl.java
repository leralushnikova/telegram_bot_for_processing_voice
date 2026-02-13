package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.service.AudioValidator;
import org.springframework.stereotype.Service;

import static com.telegram_bot_for_processing_voice.util.Constants.MAX_DURATION_SECONDS;
import static com.telegram_bot_for_processing_voice.util.Constants.MAX_FILE_SIZE_BYTES;
import static com.telegram_bot_for_processing_voice.util.Constants.SUPPORTED_AUDIO_EXTENSIONS;
import static com.telegram_bot_for_processing_voice.util.Constants.SUPPORTED_MIME_TYPES;

/**
 * Сервис для валидации аудиофайлов перед обработкой.
 */
@Service
public class AudioValidatorImpl implements AudioValidator {

    @Override
    public boolean isSupportedAudioFormat(String fileName, String mimeType) {
        if (fileName != null) {
            String extension = getFileExtension(fileName).toLowerCase();
            if (SUPPORTED_AUDIO_EXTENSIONS.contains(extension)) {
                return true;
            }
        }

        return mimeType != null && SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase());
    }

    @Override
    public boolean isValidFileSize(Long fileSize) {
        return fileSize != null && fileSize <= MAX_FILE_SIZE_BYTES;
    }

    @Override
    public boolean isValidDuration(Integer duration) {
        return duration != null && duration <= MAX_DURATION_SECONDS;
    }

    /**
     * Извлекает расширение файла.
     *
     * @param fileName имя файла
     * @return возвращает расширение файла
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}