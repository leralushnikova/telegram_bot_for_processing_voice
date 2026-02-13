package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.model.enums.Action;
import com.telegram_bot_for_processing_voice.service.StatAnalyzeService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.telegram_bot_for_processing_voice.model.enums.Action.ASSIST;
import static com.telegram_bot_for_processing_voice.model.enums.Action.INTERCEPTION;
import static com.telegram_bot_for_processing_voice.model.enums.Action.OUTLINE;
import static com.telegram_bot_for_processing_voice.model.enums.Action.SELECTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование методов сервиса ExcelStatsServiceImpl.")
class ExcelStatsServiceImplTest {

    @InjectMocks
    ExcelStatsServiceImpl excelStatsService;

    @Mock
    StatAnalyzeService statAnalyzeService;

    Map<String, Map<Action, Integer>> stats;

    @BeforeEach
    void setUp(){
        String[] players = {"Лушников", "Гук", "Григорян"};
        stats = new HashMap<>();
        for (String player : players) {
            stats.put(player, new HashMap<>());
            stats.get(player).put(ASSIST, 1);
            stats.get(player).put(OUTLINE, 1);
            stats.get(player).put(SELECTION, 1);
            stats.get(player).put(INTERCEPTION, 1);
        }

    }

    @SneakyThrows
    @Test
    @DisplayName("Проверка создания файла")
    void createExcelStatsFileSuccess(){
        Long chatId = 1L;
        String text = "Лушников передача "  +
                "Лушников обводка " +
                "Лушников отбор " +
                "Лушников перехват " +
                "Гук передача " +
                "Гук обводка " +
                "Гук отбор " +
                "Гук перехват " +
                "Григорян передача " +
                "Григорян обводка " +
                "Григорян отбор " +
                "Григорян перехват ";

        text = text.toLowerCase();

        when(statAnalyzeService.parseGameText(text)).thenReturn(stats);

        File excelFile = excelStatsService.createExcelStatsFile(chatId, text);

        assertThat(excelFile).isNotEmpty();
        assertThat(excelFile.length()).isPositive();
        assertThat(excelFile.getName()).matches("stats_.+\\.xlsx");

        verify(statAnalyzeService).parseGameText(text);
    }

    @SneakyThrows
    @Test
    @DisplayName("Проверка создания файла")
    void createExcelStatsFileFailed(){
        Long chatId = 1L;
        String text = "";

        when(statAnalyzeService.parseGameText(text)).thenReturn(new HashMap<>());

        assertThatThrownBy(() -> excelStatsService.createExcelStatsFile(chatId, text))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Статистика пуста");

        verify(statAnalyzeService).parseGameText(text);
    }
}