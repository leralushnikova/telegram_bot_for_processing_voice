package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.dto.OperationDTO;
import com.telegram_bot_for_processing_voice.dto.RecognitionDTO;
import com.telegram_bot_for_processing_voice.dto.RecognitionTextDTO;
import com.telegram_bot_for_processing_voice.dto.response.Alternative;
import com.telegram_bot_for_processing_voice.dto.response.Chunk;
import com.telegram_bot_for_processing_voice.dto.response.RecognitionResponse;
import com.telegram_bot_for_processing_voice.feign.YandexCloudOperationClient;
import com.telegram_bot_for_processing_voice.feign.YandexCloudTranscribeClient;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование методов сервиса YandexSpeechRecognitionService.")
class YandexSpeechRecognitionServiceTest {

    @InjectMocks
    YandexSpeechRecognitionService yandexSpeechRecognitionService;

    @Mock
    private YandexCloudTranscribeClient yandexCloudTranscribeClient;

    @Mock
    private YandexCloudOperationClient yandexCloudOperationClient;

    private final String testUri = "https://storage.yandexcloud.net/bucket/audio.opus";
    private final String testOperationId = "test-operation-id-123";
    private final String testLanguage = "ru-RU";
    private final Integer testDuration = 30;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(yandexSpeechRecognitionService, "defaultLanguage", testLanguage);
    }

    @Test
    @DisplayName("Тест на успешное распознавание речи.")
    void getTextFromVoice_Success() throws InterruptedException {
        String expectedText = "привет, как дела?";

        OperationDTO mockOperationDTO = new OperationDTO();
        mockOperationDTO.setId(testOperationId);
        mockOperationDTO.setDone(true);

        when(yandexCloudTranscribeClient.getOperation(any(RecognitionDTO.class)))
                .thenReturn(ResponseEntity.ok(mockOperationDTO));

        Alternative alternative = new Alternative();
        alternative.setText(expectedText);

        Chunk chunk = new Chunk();
        chunk.setAlternatives(Collections.singletonList(alternative));

        RecognitionResponse recognitionResponse = new RecognitionResponse();
        recognitionResponse.setChunks(Collections.singletonList(chunk));

        RecognitionTextDTO mockRecognitionTextDTO = new RecognitionTextDTO();
        mockRecognitionTextDTO.setDone(true);
        mockRecognitionTextDTO.setResponse(recognitionResponse);

        when(yandexCloudOperationClient.getResultText(testOperationId))
                .thenReturn(ResponseEntity.ok(mockRecognitionTextDTO));

        String actualText = yandexSpeechRecognitionService.getTextFromVoice(testUri, testDuration);

        assertNotNull(actualText, "Распознанный текст не должен быть null");
        assertEquals(expectedText, actualText, "Текст должен совпадать с ожидаемым");

        verify(yandexCloudTranscribeClient)
                .getOperation(any(RecognitionDTO.class));
        verify(yandexCloudOperationClient)
                .getResultText(testOperationId);

        verify(yandexCloudTranscribeClient).getOperation(argThat(recognitionDTO ->
                recognitionDTO.getAudio() != null &&
                        testUri.equals(recognitionDTO.getAudio().getUri()) &&
                        recognitionDTO.getConfig() != null &&
                        recognitionDTO.getConfig().getSpecification() != null &&
                        testLanguage.equals(recognitionDTO.getConfig().getSpecification().getLanguageCode())
        ));
    }

    @Test
    @DisplayName("Тест на обработку ошибки 404 при получении результата.")
    void getTextFromVoice_FeignException(){
        OperationDTO mockOperationDTO = new OperationDTO();
        mockOperationDTO.setId(testOperationId);
        mockOperationDTO.setDone(true);

        FeignException feignException = FeignException.errorStatus(
                "testMethod",
                Response.builder()
                        .status(404)
                        .reason("Not Found")
                        .headers(new HashMap<>())
                        .body("{\"error\":\"Operation not found\"}", StandardCharsets.UTF_8)
                        .request(Request.create(
                                Request.HttpMethod.GET,
                                "/operations/" + testOperationId,
                                new HashMap<>(),
                                null,
                                StandardCharsets.UTF_8,
                                null
                        ))
                        .build()
        );

        when(yandexCloudTranscribeClient.getOperation(any(RecognitionDTO.class)))
                .thenReturn(ResponseEntity.ok(mockOperationDTO));

        when(yandexCloudOperationClient.getResultText(testOperationId))
                .thenThrow(feignException);

        HttpClientErrorException exception = assertThrows(
                HttpClientErrorException.class,
                () -> yandexSpeechRecognitionService.getTextFromVoice(testUri, testDuration),
                "Метод должен бросить HttpClientErrorException при ошибке Feign"
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("YandexCloud"));

        verify(yandexCloudTranscribeClient)
                .getOperation(any(RecognitionDTO.class));
        verify(yandexCloudOperationClient)
                .getResultText(testOperationId);
    }

}