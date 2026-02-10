package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.dto.OperationDTO;
import com.telegram_bot_for_processing_voice.dto.RecognitionDTO;
import com.telegram_bot_for_processing_voice.dto.RecognitionTextDTO;
import com.telegram_bot_for_processing_voice.dto.request.AudioSource;
import com.telegram_bot_for_processing_voice.dto.request.RecognitionConfig;
import com.telegram_bot_for_processing_voice.dto.request.Specification;
import com.telegram_bot_for_processing_voice.feign.YandexCloudOperationClient;
import com.telegram_bot_for_processing_voice.feign.YandexCloudTranscribeClient;
import com.telegram_bot_for_processing_voice.service.SpeechRecognitionService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Сервис распознавания речи.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YandexSpeechRecognitionService implements SpeechRecognitionService {

    private static final long SAFETY_MARGIN_MS = 10_000L;

    @Value("${yandex.default-language}")
    private String defaultLanguage;

    private final YandexCloudTranscribeClient yandexCloudTranscribeClient;
    private final YandexCloudOperationClient yandexCloudOperationClient;

    @Override
    public String getTextFromVoice(String uri, Integer voiceDuration) throws InterruptedException {
        String operationId = getOperationID(uri);

        long baseSleepMs = (long) (voiceDuration / 60) * 10 * 1000 + SAFETY_MARGIN_MS;

        Thread.sleep(baseSleepMs);

        RecognitionTextDTO recognitionTextDTO;
        try {
            recognitionTextDTO = yandexCloudOperationClient.getResultText(operationId).getBody();
        } catch (FeignException ex) {
            log.error("Ошибка при запросе информации в YandexCloud status: {}, message: {}",
                    ex.status(), ex.getMessage());
            throw new HttpClientErrorException(HttpStatus.valueOf(ex.status()),
                    "Ошибка при запросе информации в YandexCloud");
        }
        return recognitionTextDTO.extractText();
    }

    private String getOperationID(String uri) {
        RecognitionDTO request = RecognitionDTO.builder()
                .config(RecognitionConfig.builder()
                        .specification(Specification.builder()
                                .languageCode(defaultLanguage)
                                .build())
                        .build())
                .audio(AudioSource.builder()
                        .uri(uri)
                        .build())
                .build();

        OperationDTO operationDTO;
        try {
            operationDTO = yandexCloudTranscribeClient.getOperation(request).getBody();
        } catch (FeignException ex) {
            log.error("Ошибка при запросе информации в YandexCloud status: {}, message: {}",
                    ex.status(), ex.getMessage());
            throw new HttpClientErrorException(HttpStatus.valueOf(ex.status()),
                    "Ошибка при запросе информации в YandexCloud");
        }
        return operationDTO.getId();
    }
}