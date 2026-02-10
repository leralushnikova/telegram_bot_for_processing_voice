package com.telegram_bot_for_processing_voice.dto;

import com.telegram_bot_for_processing_voice.dto.request.AudioSource;
import com.telegram_bot_for_processing_voice.dto.request.RecognitionConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO запроса для запуска асинхронного распознавания речи.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecognitionDTO {
    private RecognitionConfig config;
    private AudioSource audio;
}