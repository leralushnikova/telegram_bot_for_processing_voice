package com.telegram_bot_for_processing_voice.service;

import java.io.File;
import java.io.IOException;

/**
 * Интерфейс для создания Excel файлов со статистикой.
 */
public interface ExcelStatsService {
    
    /**
     * Создает Excel файл со статистикой и возвращает путь к файлу.
     *
     * @param chatId идентификатор чата получателя
     * @param text   текст
     * @return временный файл Excel (XLSX) со статистикой
     */
    File createExcelStatsFile(Long chatId, String text) throws IOException;
}