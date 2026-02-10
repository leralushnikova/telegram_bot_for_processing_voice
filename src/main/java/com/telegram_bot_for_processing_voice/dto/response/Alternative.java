package com.telegram_bot_for_processing_voice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Альтернативный вариант распознанного текста.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Alternative {
    private String text;
}
