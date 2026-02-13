package com.telegram_bot_for_processing_voice.exception;

/**
 * Базовое исключение для всех ошибок, связанных с аудиофайлами.
 */
public class AudioException extends RuntimeException {

    public AudioException(String message) {
        super(message);
    }

    public AudioException(String message, Throwable cause) {
        super(message, cause);
    }
}