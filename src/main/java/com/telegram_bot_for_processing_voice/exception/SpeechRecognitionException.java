package com.telegram_bot_for_processing_voice.exception;

/**
 * Базовое исключение для всех ошибок распознавания речи.
 */
public class SpeechRecognitionException extends RuntimeException {
    
    public SpeechRecognitionException(String message) {
        super(message);
    }

    public SpeechRecognitionException(String message, Throwable cause) {
        super(message, cause);
    }
}