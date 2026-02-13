package com.telegram_bot_for_processing_voice.exception;

/**
 * Исключение, выбрасываемое при ошибках загрузки файлов в облачное хранилище.
 */
public class FileUploadException extends RuntimeException {
    
    public FileUploadException(String message) {
        super(message);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}