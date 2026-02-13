package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.exception.FileUploadException;
import com.telegram_bot_for_processing_voice.service.AudioConverterService;
import com.telegram_bot_for_processing_voice.service.FileService;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Реализация сервиса для загрузки файлов в Yandex Object Storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final S3Template s3Template;
    private final AudioConverterService audioConverterService;

    @Override
    public String uploadFileAndGetUri(InputStream inputStream, String bucketName) {
        return uploadFileAndGetUri(inputStream, bucketName, "voice.ogg");
    }

    @Override
    public String uploadFileAndGetUri(InputStream inputStream, String bucket, String originalFileName) {
        try {
            InputStream oggStream;
            if (audioConverterService.needsConversion(originalFileName)) {
                log.info("Конвертируем файл {} в OGG", originalFileName);
                oggStream = audioConverterService.convertToOgg(inputStream, originalFileName);
            } else {
                log.debug("Файл {} уже в формате OGG, пропускаем конвертацию", originalFileName);
                oggStream = inputStream;
            }

            String fileName = generateUniqueFileName();
            String filePath = "telegram/voices/" + fileName;

            URI uri = s3Template.upload(bucket, filePath, oggStream).getURI();
            log.info("Файл загружен в {}", uri);

            oggStream.close();

            return uri.toString();

        } catch (IOException ex) {
            log.error("Ошибка при загрузке файла в Yandex Cloud: {}", ex.getMessage());
            throw new FileUploadException("Ошибка при загрузке файла в Yandex Cloud", ex);
        }
    }

    /**
     * Генерирует уникальное имя файла с расширением .ogg.
     *
     * @return возвращает сгенерированное имя файла.
     */
    private String generateUniqueFileName() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s_%s_%s.%s", "audio", timestamp, uuid, "ogg");
    }
}