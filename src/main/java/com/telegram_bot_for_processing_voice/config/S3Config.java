package com.telegram_bot_for_processing_voice.config;

import io.awspring.cloud.autoconfigure.s3.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * Конфигурационный файл по настройке работы с Yandex Cloud Object Storage.
 */
@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final AwsCredentialsProvider awsCredentialsProperties;
    private final AwsRegionProvider awsRegionProperties;
    private final S3Properties s3Properties;

    /**
     * Операция подключения к хранилищу Yandex Cloud Object Storage.
     *
     * @return бин S3Client.
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(awsRegionProperties.getRegion())
                .credentialsProvider(awsCredentialsProperties)
                .endpointOverride(s3Properties.getEndpoint())
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }
}