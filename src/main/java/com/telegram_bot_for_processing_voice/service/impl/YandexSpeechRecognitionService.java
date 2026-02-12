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
    private static final long POLLING_INTERVAL_MS = 2_000L;
    private static final int MAX_ATTEMPTS = 30;

    @Value("${yandex.default-language}")
    private String defaultLanguage;

    private final YandexCloudTranscribeClient yandexCloudTranscribeClient;
    private final YandexCloudOperationClient yandexCloudOperationClient;

    @Override
    public String getTextFromVoice(String uri, Integer voiceDuration) {
        String operationId = getOperationID(uri);

        if (operationId == null || operationId.isBlank()) {
            throw new IllegalStateException("Не удалось получить operationId");
        }

        long baseSleepMs = (long) (voiceDuration / 60) * 10 * 1000 + SAFETY_MARGIN_MS;

        try {
            log.debug("🎤 Аудио {} сек, первичное ожидание: {} мс ({} сек + 10 сек)",
                    voiceDuration, baseSleepMs, voiceDuration / 60 * 10);
            Thread.sleep(baseSleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Прерывание первичного ожидания", e);
        }

        int attempt = 0;

        while (attempt++ < MAX_ATTEMPTS) {

            try {
                RecognitionTextDTO recognitionTextDTO = yandexCloudOperationClient.getResultText(operationId).getBody();

                if (recognitionTextDTO == null) {
                    log.debug("Получен null ответ для operationId: {}", operationId);
                    Thread.sleep(POLLING_INTERVAL_MS);
                    continue;
                }

                if (recognitionTextDTO.getDone()) {
                    return recognitionTextDTO.extractText();
                }

                log.debug("🔄 Попытка {}/{}", attempt, MAX_ATTEMPTS);
                Thread.sleep(POLLING_INTERVAL_MS);

            } catch (FeignException ex) {
                log.error("Ошибка при запросе информации о получении данных расшифровки " +
                                "в YandexCloud status: {}, message: {}",
                        ex.status(), ex.getMessage());
                throw new HttpClientErrorException(HttpStatus.valueOf(ex.status()),
                        "Ошибка при запросе информации о получении данных расшифровки в YandexCloud");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Прерывание ожидания результата", e);
            }
        }

        throw new RuntimeException(String.format(
                "❌ Не удалось распознать за %d попыток. Аудио: %d сек",
                MAX_ATTEMPTS, voiceDuration));
    }

    /**
     * Обработка аудиофайла.
     *
     * @param uri ссылка на аудио файл.
     * @return возвращает идентификатор операции распознавания.
     */
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

        try {
            OperationDTO operationDTO = yandexCloudTranscribeClient.getOperation(request).getBody();

            if (operationDTO == null) {
                log.error("Получен null OperationDTO от YandexCloud");
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Пустое тело ответа от YandexCloud");
            }

            return operationDTO.getId();
        } catch (FeignException ex) {
            log.error("Ошибка при запросе операции в YandexCloud status: {}, message: {}",
                    ex.status(), ex.getMessage());
            throw new HttpClientErrorException(HttpStatus.valueOf(ex.status()),
                    "Ошибка при запросе операции в YandexCloud");
        }
    }
}