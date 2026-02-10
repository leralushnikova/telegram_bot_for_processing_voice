package com.telegram_bot_for_processing_voice.dto;

import com.telegram_bot_for_processing_voice.dto.response.RecognitionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO ответа с результатами распознавания речи.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecognitionTextDTO {
    private Boolean done;
    private RecognitionResponse response;

    /**
     * Проверяет, готова ли операция к извлечению текста.
     *
     * @return возвращает true, если есть текст обработки.
     */
    public boolean isReadyForTextExtraction() {
        return Boolean.TRUE.equals(done) &&
                response != null &&
                response.getChunks() != null &&
                !response.getChunks().isEmpty();
    }

    /**
     * Извлекает текст из ответа.
     *
     * @return возвращает текст обработки.
     */
    public String extractText() {
        if (isReadyForTextExtraction()) {
            return response.getChunks().get(0)
                    .getAlternatives().get(0)
                    .getText();
        }
        return null;
    }
}
