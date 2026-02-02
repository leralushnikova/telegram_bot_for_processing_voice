package com.telegram_bot_for_processing_voice.feign;

import com.telegram_bot_for_processing_voice.dto.YandexCloudTokenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Клиент для управления токенами платформы YandexCloud.
 */
@FeignClient(name = "yandexCloudTokenClient", url = "${yandex.token.url}")
public interface YandexCloudTokenClient {

    /**
     * Запрос на генерацию токена.
     *
     * @param metadataFlavor методанные
     * @return {@link YandexCloudTokenDTO}, содержащий токены: access
     */
    @PostMapping
    ResponseEntity<YandexCloudTokenDTO> generateToken(
            @RequestHeader("Metadata-Flavor") String metadataFlavor
    );
}
