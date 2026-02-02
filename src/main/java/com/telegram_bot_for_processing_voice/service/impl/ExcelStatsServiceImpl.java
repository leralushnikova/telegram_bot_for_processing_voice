package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.model.enums.Action;
import com.telegram_bot_for_processing_voice.service.ExcelStatsService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для создания Excel файлов со статистикой.
 */
@Service
public class ExcelStatsServiceImpl implements ExcelStatsService {
    
    @Override
    public File createExcelStatsFile(Map<String, Map<Action, Integer>> stats)
            throws IOException {
        
        if (stats.isEmpty()) {
            throw new IllegalArgumentException("Статистика пуста");
        }
        
        File tempFile = Files.createTempFile("stats_", ".xlsx").toFile();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Статистика");
            
            createSimpleHeaders(sheet);
            
            fillSimpleData(sheet, stats);
            
            for (int i = 0; i < Action.values().length + 2; i++) {
                sheet.autoSizeColumn(i);
            }
            
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
        }
        
        return tempFile;
    }

    /**
     * Создает заголовки таблицы в указанном листе Excel.
     *
     * @param sheet лист Excel, в котором создаются заголовки
     */
    private void createSimpleHeaders(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        
        headerRow.createCell(0).setCellValue("Фамилия");
        
        Action[] actions = Action.values();
        for (int i = 0; i < actions.length; i++) {
            headerRow.createCell(i + 1).setCellValue(actions[i].getName());
        }
        
        headerRow.createCell(actions.length + 1).setCellValue("Всего");
    }

    /**
     * Заполняет лист Excel данными статистики игроков.
     *
     * @param sheet лист Excel для заполнения данными
     * @param stats карта статистики игроков
     */
    private void fillSimpleData(Sheet sheet, Map<String, Map<Action, Integer>> stats) {
        Action[] actions = Action.values();
        int rowNum = 1;
        
        List<Map.Entry<String, Map<Action, Integer>>> sortedEntries = new ArrayList<>(stats.entrySet());
        sortedEntries.sort((a, b) -> {
            int totalA = a.getValue().values().stream().mapToInt(Integer::intValue).sum();
            int totalB = b.getValue().values().stream().mapToInt(Integer::intValue).sum();
            return Integer.compare(totalB, totalA);
        });
        
        for (Map.Entry<String, Map<Action, Integer>> entry : sortedEntries) {
            String player = entry.getKey();
            Map<Action, Integer> playerStats = entry.getValue();
            
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(player);
            
            int playerTotal = 0;
            
            for (int i = 0; i < actions.length; i++) {
                Action action = actions[i];
                int count = playerStats.getOrDefault(action, 0);
                row.createCell(i + 1).setCellValue(count);
                playerTotal += count;
            }
            
            row.createCell(actions.length + 1).setCellValue(playerTotal);
            rowNum++;
        }
        
        addSimpleTotalRow(sheet, stats, rowNum);
    }

    /**
     * Добавляет итоговую строку в таблицу Excel.
     *
     * @param sheet лист Excel, в который добавляется итоговая строка
     * @param stats карта статистики игроков для подсчета итогов
     * @param rowNum номер строки, в которую добавляются итоги
     */
    private void addSimpleTotalRow(Sheet sheet, Map<String, Map<Action, Integer>> stats, int rowNum) {
        Row totalRow = sheet.createRow(rowNum);
        Action[] actions = Action.values();
        
        totalRow.createCell(0).setCellValue("Итого");
        
        Map<Action, Integer> totals = new EnumMap<>(Action.class);
        Arrays.stream(Action.values()).forEach(action -> totals.put(action, 0));
        
        stats.values().forEach(playerStats ->
                playerStats.forEach((action, count) ->
                totals.put(action, totals.get(action) + count)));
        
        int grandTotal = 0;
        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            int total = totals.get(action);
            totalRow.createCell(i + 1).setCellValue(total);
            grandTotal += total;
        }
        
        totalRow.createCell(actions.length + 1).setCellValue(grandTotal);
        
        for (int i = 0; i < actions.length + 2; i++) {
            Cell cell = totalRow.getCell(i);
            CellStyle style = sheet.getWorkbook().createCellStyle();
            Font font = sheet.getWorkbook().createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }
    }
}