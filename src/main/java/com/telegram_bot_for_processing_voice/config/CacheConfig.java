package com.telegram_bot_for_processing_voice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурационный класс для настройки кэширования в приложении.
 */
@Configuration
@EnableCaching
@Profile("!test")
public class CacheConfig {

    private static final int EXPIRES_IN_TOKEN = 39944;

    @Value("${cache.yandexCloudToken}")
    private String yandexCloudToken;

    /**
     * Конфигурирует и возвращает {@link CaffeineCacheManager} с пользовательскими настройками кэша.
     * yandexCloudToken: Кэш со временем жизни в 39944 секунд
     *
     * @return сконфигурированный экземпляр {@link CaffeineCacheManager}.
     */
    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache(yandexCloudToken,
                Caffeine.newBuilder().expireAfterWrite(EXPIRES_IN_TOKEN, TimeUnit.SECONDS).build());
        return cacheManager;
    }

}