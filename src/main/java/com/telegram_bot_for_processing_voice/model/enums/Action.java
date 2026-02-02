package com.telegram_bot_for_processing_voice.model.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Перечисление допустимых игровых действий.
 */
@Getter
public enum Action {
    ASSIST("передача"), OUTLINE("обводка"), INTERCEPTION("перехват"), SELECTION("отбор");
    private static final Map<String, Action> BY_NAME = new HashMap<>();

    private final String name;

    static {
        for (Action action : values()) {
            BY_NAME.put(action.name.toLowerCase(), action);
        }
    }

    Action(String name) {
        this.name = name;
    }

    /**
     * Объединяет строку со всеми перечислениями.
     *
     * @return возвращает строку регулярного выражения.
     */
    public static String getAllNamesRegex() {
        return BY_NAME.keySet().stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
    }

    /**
     * Находит действие по названию.
     *
     * @param name - название действия.
     * @return возвращает действие.
     */
    public static Optional<Action> getAction(String name) {
        return Optional.ofNullable(BY_NAME.get(name.toLowerCase()));
    }
}
