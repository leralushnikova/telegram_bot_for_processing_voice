package com.telegram_bot_for_processing_voice.dto;

/**
 * DTO для представления результат от API YandexCloud.
 *
 * @param result - текст распознавания.
 */
public record YandexCloudDTO(String result) {
}
