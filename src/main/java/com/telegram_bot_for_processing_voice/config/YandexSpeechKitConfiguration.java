package com.telegram_bot_for_processing_voice.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Конфигурация Feign-клиентов для работы с сервисом YandexSpeechKit.
 */
@Slf4j
public class YandexSpeechKitConfiguration {

    private static final String AUTH_TOKEN_TYPE = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";

    @Value("${connection.yandex.token}")
    private String token;

    /**
     * Создает interceptor для Feign-запросов в YandexSpeechKit, который добавляет заголовок
     * Authorization с JWT-токеном в каждый исходящий запрос.
     * Также логируется URL каждого исходящего запроса.
     *
     * @return возвращает экземпляр {@link RequestInterceptor}.
     */
    @Bean
    public RequestInterceptor openAIRequestInterceptor() {
        return requestTemplate -> {

            requestTemplate.header(AUTHORIZATION, AUTH_TOKEN_TYPE + token);

            log.info("Feign запрос к: {}", requestTemplate.url());
        };
    }
}
