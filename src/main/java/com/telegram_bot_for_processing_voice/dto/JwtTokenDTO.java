package com.telegram_bot_for_processing_voice.dto;

/**
 * DTO запрос на получение IAM-токена Yandex Cloud.
 *
 * @param jwt - токен.
 */
public record JwtTokenDTO(String jwt) {
}