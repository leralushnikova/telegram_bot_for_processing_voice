package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.dto.YandexCloudDTO;
import com.telegram_bot_for_processing_voice.feign.YandexCloudClient;
import com.telegram_bot_for_processing_voice.service.SpeechRecognitionService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexSpeechRecognitionService implements SpeechRecognitionService {

    @Value("${yandex.folder-id}")
    private String folderId;

    @Value("${yandex.default-language}")
    private String defaultLanguage;

    private final YandexCloudClient yandexCloudClient;

    @Override
    public String recognizeSpeech(byte[] audioData) {
        YandexCloudDTO yandexCloudDTO;
        try {
            yandexCloudDTO = yandexCloudClient.createTextFromVoice(folderId, defaultLanguage, audioData).getBody();
        } catch (FeignException ex) {
            log.error("Ошибка при запросе информации в YandexSpeechKit", ex);
            throw new HttpClientErrorException(HttpStatus.valueOf(ex.status()),
                    "Ошибка при запросе информации в YandexSpeechKit");
        }
        return yandexCloudDTO.result();
    }
}