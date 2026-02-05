package com.telegram_bot_for_processing_voice.service.impl.token;

import com.telegram_bot_for_processing_voice.dto.JwtTokenDTO;
import com.telegram_bot_for_processing_voice.dto.YandexCloudTokenDTO;
import com.telegram_bot_for_processing_voice.feign.YandexCloudTokenClient;
import com.telegram_bot_for_processing_voice.service.JwtService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

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
        YandexCloudTokenDTO token;
        try {
            token = yandexCloudTokenClient.generateToken(jwtTokenDTO).getBody();
        } catch (FeignException ex) {
            log.error("Ошибка при запросе токена в YandexSpeechKit status: {}",
                    ex.status());
            throw new HttpClientErrorException(HttpStatus.valueOf(ex.status()),
                    "Ошибка при запросе токена в YandexSpeechKit");
        }
        log.info("Токен успешно получен для пользователя userId={}", userId);
        return token;
    }
}
