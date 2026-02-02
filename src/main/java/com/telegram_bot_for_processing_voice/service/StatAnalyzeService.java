package com.telegram_bot_for_processing_voice.service;

import com.telegram_bot_for_processing_voice.model.enums.Action;

import java.util.Map;

/**
 * Сервис для анализа статистики игроков из текста игрового отчета.
 */
public interface StatAnalyzeService {

    /**
     * Парсит текст игры и возвращает статистику в виде структурированных данных.
     * @param text - текст игрового отчета для анализа
     * @return возвращает мапу статистики.
     */
    Map<String, Map<Action, Integer>> parseGameText(String text);
}
