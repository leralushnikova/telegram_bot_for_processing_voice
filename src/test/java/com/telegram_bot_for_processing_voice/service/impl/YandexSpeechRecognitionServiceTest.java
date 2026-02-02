package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.dto.YandexCloudDTO;
import com.telegram_bot_for_processing_voice.feign.YandexCloudClient;
import feign.FeignException;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование методов сервиса YandexSpeechRecognitionService.")
class YandexSpeechRecognitionServiceTest {

    @InjectMocks
    YandexSpeechRecognitionService yandexSpeechRecognitionService;

    @Mock
    YandexCloudClient yandexCloudClient;

    @Mock
    FeignException feignException;

    @Test
    @DisplayName("Проверка распознавания речи")
    void recognizeSpeechSuccess() {
        YandexCloudDTO yandexCloudDTO = Instancio.create(YandexCloudDTO.class);
        ResponseEntity<YandexCloudDTO> responseEntity =
                new ResponseEntity<>(yandexCloudDTO, HttpStatus.OK);

        byte[] audioData = new byte[1024];
        Arrays.fill(audioData, (byte) 1);
        String folderId = "folderId";
        String lang = "ru-Ru";

        ReflectionTestUtils.setField(yandexSpeechRecognitionService, "folderId", folderId);
        ReflectionTestUtils.setField(yandexSpeechRecognitionService, "defaultLanguage", lang);

        when(yandexCloudClient.createTextFromVoice(eq(folderId), eq(lang), same(audioData))).thenReturn(responseEntity);

        String result = yandexSpeechRecognitionService.recognizeSpeech(audioData);

        assertThat(result).isEqualTo(yandexCloudDTO.result());

        verify(yandexCloudClient).createTextFromVoice(eq(folderId), eq(lang), same(audioData));
    }

    @Test
    @DisplayName("Проверка распознавания речи")
    void recognizeSpeechFailed() {
        byte[] audioData = new byte[1024];
        Arrays.fill(audioData, (byte) 1);
        String folderId = "folderId";
        String lang = "ru-Ru";

        ReflectionTestUtils.setField(yandexSpeechRecognitionService, "folderId", folderId);
        ReflectionTestUtils.setField(yandexSpeechRecognitionService, "defaultLanguage", lang);

        when(feignException.status()).thenReturn(400);
        when(yandexCloudClient.createTextFromVoice(eq(folderId), eq(lang), same(audioData))).thenThrow(feignException);

        assertThatThrownBy(() -> yandexSpeechRecognitionService.recognizeSpeech(audioData))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessage("400 Ошибка при запросе информации в YandexSpeechKit");

        verify(yandexCloudClient).createTextFromVoice(eq(folderId), eq(lang), same(audioData));
        verifyNoMoreInteractions(yandexCloudClient);
    }
}