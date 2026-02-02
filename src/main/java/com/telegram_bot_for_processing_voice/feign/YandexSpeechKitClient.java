package com.telegram_bot_for_processing_voice.feign;

import com.telegram_bot_for_processing_voice.config.YandexSpeechKitConfiguration;
import com.telegram_bot_for_processing_voice.dto.YandexSpeechKitDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign-клиент для взаимодействия с внешним API платформы YandexSpeech.
 */
@FeignClient(name = "yandexSpeechKitClient",
        url = "${connection.yandex.url}",
        configuration = YandexSpeechKitConfiguration.class)
public interface YandexSpeechKitClient {

    /**
     * Операция преобразования аудио через YandexSpeech.
     *
     * @param folderId - идентификатор каталога
     * @param lang - язык, для которого будет выполнено распознавание
     * @param audioData - двоичное содержимое аудиофайла
     * @return возвращает объект YandexSpeechKitDTO.
     */
    @PostMapping(value = "/speech/v1/stt:recognize",
            consumes = "application/octet-stream")
    ResponseEntity<YandexSpeechKitDTO> createTextFromVoice(
            @RequestParam("folderId") String folderId,
            @RequestParam("lang") String lang,
            @RequestBody byte[] audioData
    );
}
