package com.telegram_bot_for_processing_voice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Фрагмент (чанк) распознанного аудио.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chunk {
    private List<Alternative> alternatives;
}