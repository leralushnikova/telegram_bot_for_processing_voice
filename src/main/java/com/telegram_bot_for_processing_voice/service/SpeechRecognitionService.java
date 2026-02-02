package com.telegram_bot_for_processing_voice.service;

/**
 * Интерфейс распознавания речи.
 */
public interface SpeechRecognitionService {

    /**
     * Распознать речь с автоопределением языка.
     *
     * @param audioData audioData байты аудио файла
     * @return распознанный текст
     */
    String recognizeSpeech(byte[] audioData) throws Exception;
}