package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.model.enums.Action;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование методов сервиса ExcelStatsServiceImpl.")
class ExcelStatsServiceImplTest {

    @InjectMocks
    ExcelStatsServiceImpl excelStatsService;

    Map<String, Map<Action, Integer>> stats;

    @BeforeEach
    void setUp(){
        String[] players = {"Лушников", "Гук", "Григорян"};
        stats = new HashMap<>();
        for (String player : players) {
            stats.put(player, new HashMap<>());
            stats.get(player).put(ASSIST, 2);
            stats.get(player).put(OUTLINE, 3);
            stats.get(player).put(SELECTION, 1);
            stats.get(player).put(INTERCEPTION, 1);
        }

    }

    @SneakyThrows
    @Test
    @DisplayName("Проверка создания файла")
    void createExcelStatsFileSuccess(){

        File excelFile = excelStatsService.createExcelStatsFile(stats);

        assertThat(excelFile).isNotEmpty();
        assertThat(excelFile.length()).isPositive();
        assertThat(excelFile.getName()).matches("stats_.+\\.xlsx");

        excelFile.delete();
    }

    @SneakyThrows
    @Test
    @DisplayName("Проверка создания файла")
    void createExcelStatsFileFailed(){

        assertThatThrownBy(() -> excelStatsService.createExcelStatsFile(new HashMap<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Статистика пуста");

    }

}