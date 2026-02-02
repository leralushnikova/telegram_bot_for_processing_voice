package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.model.enums.Action;
import com.telegram_bot_for_processing_voice.service.StatAnalyzeService;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Реализация сервиса для анализа статистики игроков.
 */
@Service
public class StatAnalyzeServiceImpl implements StatAnalyzeService {

    /**
     * Регулярное выражение для поиска пар "Игрок Действие".
     */
    private static final Pattern PLAYER_ACTION_PATTERN =
            Pattern.compile("([а-яёА-ЯЁ]+)\\s+(" + Action.getAllNamesRegex() + ")",
                    Pattern.CASE_INSENSITIVE);

    @Override
    public Map<String, Map<Action, Integer>> parseGameText(String text) {
        Map<String, Map<Action, Integer>> stats = new HashMap<>();

        Matcher matcher = PLAYER_ACTION_PATTERN.matcher(text);

        while (matcher.find()) {
            String player = matcher.group(1);
            String actionName = matcher.group(2);

            Action.getAction(actionName).ifPresent(action -> {
                stats.putIfAbsent(player, new EnumMap<>(Action.class));
                Map<Action, Integer> playerStats = stats.get(player);
                playerStats.put(action, playerStats.getOrDefault(action, 0) + 1);
            });
        }

        return stats;
    }
}
