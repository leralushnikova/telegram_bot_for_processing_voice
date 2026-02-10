package com.telegram_bot_for_processing_voice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Ответ с результатами распознавания речи.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecognitionResponse {
    private List<Chunk> chunks;
}