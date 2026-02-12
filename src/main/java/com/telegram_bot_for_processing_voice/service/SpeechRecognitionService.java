package com.telegram_bot_for_processing_voice.service;

/**
 * Интерфейс распознавания речи.
 */
public interface SpeechRecognitionService {

    /**
     * Распознает речь из аудиофайла, расположенного по-указанному URI.
     *
     * @param uri URI аудиофайла в облачном хранилище.
     * @param voiceDuration длительность аудиофайла в секундах.
     * @return распознанный текст из аудиофайла или null, если распознавание не удалось
     */
    String getTextFromVoice(String uri, Integer voiceDuration);
}