package com.telegram_bot_for_processing_voice.service.conf;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Тестовая конфигурация для настройки кэширования в тестах.
 */
@Configuration
@EnableCaching
public class TestYandexCloudConfig {

    /**
     * Создает и настраивает {@link CacheManager} для работы с кэшем в тестах.
     *
     * @return Экземпляр {@link CacheManager} с заданными параметрами.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(100));
        return cacheManager;
    }
}