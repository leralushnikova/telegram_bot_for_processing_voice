package com.telegram_bot_for_processing_voice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Представляет источник аудиоданных для распознавания речи.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioSource {
    private String uri;
}