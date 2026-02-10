package com.telegram_bot_for_processing_voice.feign;

import com.telegram_bot_for_processing_voice.config.YandexCloudConfiguration;
import com.telegram_bot_for_processing_voice.dto.RecognitionTextDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign-клиент для асинхронного распознавания аудиофайлов в формате OggOpus в API v2 с YandexCloud для получения результата.
 */
@FeignClient(name = "yandexCloudOperationClient",
        url = "${yandex.operation.url}",
        configuration = YandexCloudConfiguration.class)
public interface YandexCloudOperationClient {

    /**
     * Получения расшифровки.
     *
     * @param operationId идентификатор операции распознавания.
     * @return возвращает текст.
     */
    @GetMapping(
            value = "/operations/{operationId}")
    ResponseEntity<RecognitionTextDTO> getResultText(
            @PathVariable String operationId
    );
}