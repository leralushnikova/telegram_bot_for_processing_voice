package com.telegram_bot_for_processing_voice.handler;

import com.telegram_bot_for_processing_voice.bot.MessageSender;
import com.telegram_bot_for_processing_voice.bot.SpeechRecognitionBot;
import com.telegram_bot_for_processing_voice.service.ExcelStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.File;

/**
 * Обработчик сообщений в Telegram боте.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TextMessageHandler implements MessageHandler {

    private final ExcelStatsService excelStatsService;
    private final MessageSender messageSender;

    @Override
    public boolean canHandle(Message message) {
        return message.hasText();
    }

    @Override
    public void handle(Message message, SpeechRecognitionBot bot) {
        Long chatId = message.getChatId();
        String text = message.getText();

        try {
            messageSender.sendTyping(bot, chatId);

            messageSender.sendText(bot, chatId, "Обрабатываю сообщение...");

            File excelFile = excelStatsService.createExcelStatsFile(chatId, text);

            messageSender.sendExcel(bot, chatId, excelFile);

            excelFile.delete();

        } catch (Exception e) {
            log.error("Ошибка обработки голосового сообщения", e);
            messageSender.sendError(bot, chatId, "❌ Ошибка при обработке голосового сообщения: " + e.getMessage());
        }
    }
}