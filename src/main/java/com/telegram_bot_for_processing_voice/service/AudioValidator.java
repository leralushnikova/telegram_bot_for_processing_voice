package com.telegram_bot_for_processing_voice.service;

/**
 * Интерфейс для валидации аудиофайлов перед обработкой.
 */
public interface AudioValidator {
    /**
     * Проверяет, поддерживается ли аудиоформат.
     *
     * @param fileName имя файла
     * @param mimeType тип файла
     * @return возвращает true, если такое расширение файла поддерживается
     */
    boolean isSupportedAudioFormat(String fileName, String mimeType);

    /**
     * Проверяет, соответствует ли размер аудиофайла установленным ограничениям.
     *
     * @param fileSize размер файла в байтах, должен быть положительным числом
     * @return возвращает true если размер файла не превышает максимально допустимый.
     */
    boolean isValidFileSize(Long fileSize);

    /**
     * Проверяет, соответствует ли длительность аудиозаписи установленным ограничениям.
     *
     * @param duration длительность аудио в секундах, должен быть положительным числом
     * @return возвращает true если длительность аудио не превышает максимально допустимую
     */
    boolean isValidDuration(Integer duration);
}