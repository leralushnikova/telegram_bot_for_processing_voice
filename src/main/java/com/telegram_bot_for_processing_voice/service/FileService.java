package com.telegram_bot_for_processing_voice.service;

import java.io.InputStream;

/**
 * Сервис для загрузки файлов в облачное хранилище.
 */
public interface FileService {

    /**
     * Загружает файл из потока данных в указанное облачное хранилище.
     *
     * @param inputStream поток данных
     * @param bucket имя бакета
     * @return возвращает URI загруженного файла в облачном хранилище в формате строки
     */
    String uploadFileAndGetUri(InputStream inputStream, String bucket);

    /**
     * Загружает файл из потока данных в указанное облачное хранилище.
     *
     * @param inputStream поток данных
     * @param bucket имя бакета
     * @param originalFileName оригинальное имя файла
     * @return возвращает URI загруженного файла в облачном хранилище в формате строки
     */
    String uploadFileAndGetUri(InputStream inputStream, String bucket, String originalFileName);
}
