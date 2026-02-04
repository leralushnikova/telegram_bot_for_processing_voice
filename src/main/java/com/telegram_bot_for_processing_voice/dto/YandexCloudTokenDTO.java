package com.telegram_bot_for_processing_voice.dto;

/**
 * DTO ответ от API Yandex Cloud с IAM-токеном.
 *
 * @param iamToken - токен.
 */
public record YandexCloudTokenDTO(String iamToken) {
}
