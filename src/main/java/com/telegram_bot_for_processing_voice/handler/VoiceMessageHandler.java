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
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Voice;

import java.io.File;
import java.io.InputStream;

/**
 * Голосовой обработчик сообщений в Telegram боте.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceMessageHandler implements MessageHandler {

    private final AudioValidator audioValidator;
    private final FileService fileService;
    private final ExcelStatsService excelStatsService;
    private final SpeechRecognitionService speechService;
    private final TelegramFileDownloader telegramFileDownloader;
    private final MessageSender messageSender;

    @Override
    public boolean canHandle(Message message) {
        return message.hasVoice();
    }

    @Override
    public void handle(Message message, SpeechRecognitionBot bot) {
        Long chatId = message.getChatId();
        Voice voice = message.getVoice();

        try {
            messageSender.sendTyping(bot, chatId);
            messageSender.sendText(bot, chatId, "🎤 Скачиваю голосовое сообщение...");

            Integer duration = voice.getDuration();

            if (!audioValidator.isValidDuration(duration)) {
                messageSender.sendText(bot, chatId,
                        String.format("❌ Аудио слишком длинное (%d минут). Максимальная длительность: 40 минут",
                                duration / 60));
                return;
            }

            messageSender.sendText(bot, chatId, "🔍 Распознаю речь...");

            InputStream inputStream = telegramFileDownloader.downloadFileAsStream(voice.getFileId(), bot);

            String uri = fileService.uploadFileAndGetUri(inputStream, bot.getBucket());

            String recognizedText = speechService.getTextFromVoice(uri, duration);

            File excelFile = excelStatsService.createExcelStatsFile(chatId, recognizedText);

            messageSender.sendExcel(bot, chatId, excelFile);

            excelFile.delete();

        } catch (Exception e) {
            log.error("Ошибка обработки голосового сообщения", e);
            messageSender.sendError(bot, chatId, "❌ Ошибка при обработке голосового сообщения: " + e.getMessage());
        }
    }
}