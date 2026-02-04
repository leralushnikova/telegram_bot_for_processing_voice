package com.telegram_bot_for_processing_voice.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Класс получение jwt проперти значений из файла application.yml.
 */
@Component
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ConfigurationProperties(prefix = "yandex.auth")
@Profile("!test")
public class JwtProperties {
    private String id;
    private String serviceAccountId;
    private String privateKey;
}
