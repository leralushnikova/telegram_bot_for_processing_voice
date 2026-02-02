package com.telegram_bot_for_processing_voice.service;

import com.telegram_bot_for_processing_voice.model.enums.Action;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Интерфейс для создания Excel файлов со статистикой.
 */
public interface ExcelStatsService {
    
    /**
     * Создает Excel файл со статистикой и возвращает путь к файлу.
     *
     * @param stats карта статистики игроков
     * @return временный файл Excel (XLSX) со статистикой
     */
    File createExcelStatsFile(Map<String, Map<Action, Integer>> stats) throws IOException;
}