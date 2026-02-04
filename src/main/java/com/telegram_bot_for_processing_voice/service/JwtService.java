package com.telegram_bot_for_processing_voice.service;

import com.telegram_bot_for_processing_voice.dto.JwtTokenDTO;

/**
 * Интерфейс для генерации JWT токенов.
 */
public interface JwtService {

    /**
     * Генерирует JWT токен.
     * @return {@link JwtTokenDTO} содержащий сгенерированный JWT токен
     */
    JwtTokenDTO getJwtToken();
}
