package com.telegram_bot_for_processing_voice.dto;

/**
 * DTO для представления результат от API YandexSpeech.
 *
 * @param result - текст распознавания.
 */
public record YandexSpeechKitDTO(String result) {
}
