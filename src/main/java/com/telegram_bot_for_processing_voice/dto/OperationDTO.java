package com.telegram_bot_for_processing_voice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления операции асинхронного распознавания речи.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationDTO {
    private Boolean done;
    private String id;
}