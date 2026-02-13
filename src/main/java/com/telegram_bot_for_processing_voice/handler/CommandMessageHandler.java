package com.telegram_bot_for_processing_voice.handler;

import com.telegram_bot_for_processing_voice.bot.MessageSender;
import com.telegram_bot_for_processing_voice.bot.SpeechRecognitionBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.telegram_bot_for_processing_voice.util.Constants.COMMAND_EXAMPLE;
import static com.telegram_bot_for_processing_voice.util.Constants.COMMAND_HELP;
import static com.telegram_bot_for_processing_voice.util.Constants.COMMAND_START;

/**
 * Обработчик команд в Telegram боте.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommandMessageHandler implements MessageHandler {
    private final MessageSender messageSender;

    @Override
    public boolean canHandle(Message message) {
        return message.hasText() && message.getText().startsWith("/");
    }

    @Override
    public void handle(Message message, SpeechRecognitionBot bot) {
        Long chatId = message.getChatId();
        String command = message.getText();

        switch (command) {
            case COMMAND_START:
                messageSender.sendWelcome(bot, chatId, message.getFrom().getFirstName());
                break;

            case COMMAND_HELP:
                messageSender.sendHelp(bot, chatId);
                break;

            case COMMAND_EXAMPLE:
                messageSender.sendExample(bot, chatId);
                break;

            default:
                messageSender.sendText(bot, chatId, "Неизвестная команда. Используйте " + COMMAND_HELP + "  для списка команд.");
        }
    }
}