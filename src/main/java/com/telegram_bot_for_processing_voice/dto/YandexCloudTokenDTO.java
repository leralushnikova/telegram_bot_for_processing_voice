package com.telegram_bot_for_processing_voice.dto;

/**
 * DTO для представления токена от API YandexCloud.
 *
 * @param accessToken - токен.
 * @param expiresIn - оставшееся время жизни токена.
 * @param tokenType - тип токена.
 */
public record YandexCloudTokenDTO(String accessToken, Long expiresIn, String tokenType) {
}
