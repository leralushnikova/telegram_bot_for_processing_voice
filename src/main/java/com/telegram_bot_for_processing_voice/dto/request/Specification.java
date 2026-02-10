package com.telegram_bot_for_processing_voice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Спецификация параметров распознавания речи.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Specification {
    private String languageCode;
}