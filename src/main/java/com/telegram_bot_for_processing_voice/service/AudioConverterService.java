package com.telegram_bot_for_processing_voice.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * Интерфейс конвертации аудио-файлов.
 */
public interface AudioConverterService {
    
    /**
     * Конвертирует InputStream с аудио в формат OGG.
     *
     * @param inputStream исходный аудиопоток
     * @param originalFileName оригинальное имя файла (для определения формата)
     * @return InputStream с аудио в формате OGG
     */
    InputStream convertToOgg(InputStream inputStream, String originalFileName) throws IOException;
    
    /**
     * Проверяет, нужна ли конвертация в OGG.
     *
     * @param fileName имя файла
     * @return true, если необходима конвертация.
     */
    boolean needsConversion(String fileName);
}