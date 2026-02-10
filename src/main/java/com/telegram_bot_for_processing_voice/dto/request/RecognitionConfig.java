package com.telegram_bot_for_processing_voice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Конфигурация для распознавания речи, содержащая параметры обработки аудио.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecognitionConfig {
    private Specification specification;
}