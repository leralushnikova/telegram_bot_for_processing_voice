package com.telegram_bot_for_processing_voice.service.impl.token;

import com.telegram_bot_for_processing_voice.dto.JwtTokenDTO;
import com.telegram_bot_for_processing_voice.dto.YandexCloudTokenDTO;
import com.telegram_bot_for_processing_voice.feign.YandexCloudTokenClient;
import com.telegram_bot_for_processing_voice.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса для работы с токенами платформы YandexCloud.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YandexCloudTokenService {

    private final YandexCloudTokenClient yandexCloudTokenClient;
    private final JwtService jwtService;

    /**
     * Получает JWT токен для указанного пользователя и хранит его в кэше.
     *
     * @param userId уникальный идентификатор пользователя, который будет использоваться для
     *               кэширования токена.
     * @return объект {@link YandexCloudTokenDTO}, содержащий Access Token.
     * @throws IllegalStateException если сервер не вернул токены
     */
    @Cacheable(value = "yandexCloudToken", key = "#userId")
    public YandexCloudTokenDTO getIamToken(String userId) {
        log.info("Токен отсутствует в кэше. Запрос токена через YandexCloudTokenDTO для userId={}",
                userId);

        JwtTokenDTO jwtTokenDTO = jwtService.getJwtToken();
        log.info("jwt token={}", jwtTokenDTO.jwt());
        YandexCloudTokenDTO token = yandexCloudTokenClient.generateToken(jwtTokenDTO).getBody();
        if (token == null) {
            log.error("Ошибка: сервер не вернул токены для пользователя userId={}", userId);
            throw new IllegalStateException("Сервер не вернул токены для пользователя: " + userId);
        }

        log.info("Токен успешно получен для пользователя userId={}", userId);
        return token;
    }
}
