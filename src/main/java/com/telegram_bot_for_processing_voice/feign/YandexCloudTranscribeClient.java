package com.telegram_bot_for_processing_voice.feign;

import com.telegram_bot_for_processing_voice.config.YandexCloudConfiguration;
import com.telegram_bot_for_processing_voice.dto.OperationDTO;
import com.telegram_bot_for_processing_voice.dto.RecognitionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign-клиент для асинхронного распознавания аудиофайлов в формате OggOpus в API v2 с YandexCloud для обработки.
 */
@FeignClient(name = "yandexCloudTranscribeClient",
        url = "${yandex.transcribe.url}",
        configuration = YandexCloudConfiguration.class)
public interface YandexCloudTranscribeClient {

    /**
     * Обработка аудиофайла.
     *
     * @param voiceRequest объект содержит ссылку на обработку аудио.
     * @return возвращает идентификатор операции распознавания.
     */
    @PostMapping(
            value = "/speech/stt/v2/longRunningRecognize",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OperationDTO> getOperation(
            @RequestBody RecognitionDTO voiceRequest
    );
}