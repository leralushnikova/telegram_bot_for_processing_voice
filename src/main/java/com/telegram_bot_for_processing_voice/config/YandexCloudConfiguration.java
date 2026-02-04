package com.telegram_bot_for_processing_voice.config;

import com.telegram_bot_for_processing_voice.service.impl.token.YandexCloudTokenService;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Конфигурация Feign-клиентов для работы с сервисом YandexCloud.
 */
@Slf4j
public class YandexCloudConfiguration {

    private static final String AUTH_TOKEN_TYPE = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";

    @Value("${connection.yandex.token}")
    private String token;

    /**
     * Создает interceptor для Feign-запросов в YandexCloud, который добавляет заголовок
     * Authorization с JWT-токеном в каждый исходящий запрос.
     * Также логируется URL каждого исходящего запроса.
     *
     * @param yandexCloudTokenService сервис для работы с токенами платформы YandexCloud
     * @return возвращает экземпляр {@link RequestInterceptor}.
     */
    @Bean
    public RequestInterceptor openAIRequestInterceptor(YandexCloudTokenService yandexCloudTokenService) {
        return requestTemplate -> {

//            String userId =
//                    "текущий пользователь"; //TODO настроить получение уник.атрибута пользователя;

            requestTemplate.header(AUTHORIZATION, AUTH_TOKEN_TYPE + token);

//            YandexCloudTokenDTO token = yandexCloudTokenService.getJwtToken(userId);
//
//            requestTemplate.header(AUTHORIZATION, token.tokenType() + " " + token.accessToken());

            log.info("Feign запрос к: {}", requestTemplate.url());
        };
    }
}
