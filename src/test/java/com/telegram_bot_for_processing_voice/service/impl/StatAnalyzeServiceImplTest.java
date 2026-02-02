package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.model.enums.Action;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование методов сервиса StatAnalyzeServiceImpl.")
class StatAnalyzeServiceImplTest {

    @InjectMocks
    StatAnalyzeServiceImpl statAnalyzeService;

    @Test
    @DisplayName("Проверка создания статистики")
    void parseGameTextSuccess() {
        String text = "лушников обводка гук перехват гук отбор григорян перехват лушников передача лушников перехват гук отбор ";

        Map<String, Map<Action, Integer>> stats = statAnalyzeService.parseGameText(text.toLowerCase());

        assertThat(stats).isNotEmpty();
        assertThat(stats).hasSize(3);
        assertThat(stats).containsKey("лушников");
    }
}