package com.telegram_bot_for_processing_voice.feign;

import com.telegram_bot_for_processing_voice.dto.JwtTokenDTO;
import com.telegram_bot_for_processing_voice.dto.YandexCloudTokenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Клиент для управления токенами платформы YandexCloud.
 */
@FeignClient(name = "yandexCloudTokenClient", url = "${yandex.iam.url}")
public interface YandexCloudTokenClient {

    /**
     * Запрос на генерацию токена.
     *
     * @param jwt - jwt токен
     * @return возвращает объект YandexCloudTokenDTO
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<YandexCloudTokenDTO> generateToken(
            @RequestBody JwtTokenDTO jwt
    );
}
