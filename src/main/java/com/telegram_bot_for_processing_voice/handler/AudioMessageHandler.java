package com.telegram_bot_for_processing_voice.handler;

import com.telegram_bot_for_processing_voice.bot.MessageSender;
import com.telegram_bot_for_processing_voice.bot.SpeechRecognitionBot;
import com.telegram_bot_for_processing_voice.service.AudioValidator;
import com.telegram_bot_for_processing_voice.service.ExcelStatsService;
import com.telegram_bot_for_processing_voice.service.FileService;
import com.telegram_bot_for_processing_voice.service.SpeechRecognitionService;
import com.telegram_bot_for_processing_voice.service.TelegramFileDownloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Audio;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;
import java.io.InputStream;

/**
 * Аудио обработчик сообщений в Telegram боте.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AudioMessageHandler implements MessageHandler {

    private final AudioValidator audioValidator;
    private final ExcelStatsService excelStatsService;
    private final FileService fileService;
    private final SpeechRecognitionService speechService;
    private final TelegramFileDownloader telegramFileDownloader;
    private final MessageSender messageSender;

    @Override
    public boolean canHandle(Message message) {
        return message.hasAudio();
    }

    @Override
    public void handle(Message message, SpeechRecognitionBot bot) {
        Long chatId = message.getChatId();
        Audio audio = message.getAudio();

        try {
            messageSender.sendTyping(bot, chatId);
            messageSender.sendText(bot, chatId, "🎵 Скачиваю аудиофайл...");

            String fileName = audio.getFileName();
            String mimeType = audio.getMimeType();
            Integer duration = audio.getDuration();

            log.info("Получен аудиофайл: {} (MIME: {}, длительность: {} сек)",
                    fileName, mimeType, duration);

            if (!audioValidator.isSupportedAudioFormat(fileName, mimeType)) {
                messageSender.sendUnsupportedFormat(bot, chatId, fileName);
                return;
            }

            Long fileSize = audio.getFileSize();
            if (!audioValidator.isValidFileSize(fileSize)) {
                messageSender.sendText(bot, chatId,
                        String.format("❌ Файл слишком большой (%.1f MB). Максимальный размер: 50 MB",
                                fileSize / (1024.0 * 1024.0)));
                return;
            }

            if (!audioValidator.isValidDuration(duration)) {
                messageSender.sendText(bot, chatId,
                        String.format("❌ Аудио слишком длинное (%d минут). Максимальная длительность: 40 минут",
                                duration / 60));
                return;
            }

            messageSender.sendText(bot, chatId, "🔍 Распознаю речь...");

            InputStream inputStream = telegramFileDownloader.downloadFileAsStream(audio.getFileId(), bot);

            String uri = fileService.uploadFileAndGetUri(inputStream, bot.getBucket(), fileName);

            String recognizedText = speechService.getTextFromVoice(uri, duration);

            File excelFile = excelStatsService.createExcelStatsFile(chatId, recognizedText);

            messageSender.sendExcel(bot, chatId, excelFile);

            excelFile.delete();

        } catch (Exception e) {
            log.error("Ошибка обработки аудиофайла", e);
            messageSender.sendError(bot, chatId, "не удалось обработать аудиофайл");
        }
    }
}