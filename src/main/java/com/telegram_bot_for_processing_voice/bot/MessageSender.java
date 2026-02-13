package com.telegram_bot_for_processing_voice.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.telegram_bot_for_processing_voice.util.Constants.COMMAND_EXAMPLE;
import static com.telegram_bot_for_processing_voice.util.Constants.COMMAND_HELP;
import static com.telegram_bot_for_processing_voice.util.Constants.COMMAND_START;
import static com.telegram_bot_for_processing_voice.util.Constants.EXAMPLE_MESSAGE;
import static com.telegram_bot_for_processing_voice.util.Constants.HELP_MESSAGE;
import static com.telegram_bot_for_processing_voice.util.Constants.UNSUPPORTED_FORMAT_MESSAGE;
import static com.telegram_bot_for_processing_voice.util.Constants.WELCOME_MESSAGE;

/**
 * Компонент формирования сообщений в Telegram.
 */
@Slf4j
@Component
public class MessageSender {

    /**
     * Регистрирует команды бота в Telegram.
     *
     * @param bot экземпляр бота SpeechRecognitionBot
     */
    public void registerBotCommands(SpeechRecognitionBot bot) {
        try {
            List<BotCommand> commands = new ArrayList<>();
            commands.add(new BotCommand(COMMAND_START, "Начать работу с ботом"));
            commands.add(new BotCommand(COMMAND_HELP, "Помощь и инструкции"));
            commands.add(new BotCommand(COMMAND_EXAMPLE, "Пример использования"));

            bot.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
            log.info("Команды бота успешно зарегистрированы в Telegram");

        } catch (TelegramApiException e) {
            log.error("Не удалось зарегистрировать команды бота", e);
        }
    }

    /**
     * Отправляет текстовое сообщение пользователю в Telegram.
     *
     * @param bot    экземпляр бота SpeechRecognitionBot
     * @param chatId идентификатор чата получателя
     * @param text   текст сообщения для отправки
     */
    public void sendText(SpeechRecognitionBot bot, Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

    /**
     * Отправляет индикатор набора текста (typing action) в чат.
     *
     * @param bot    экземпляр бота SpeechRecognitionBot
     * @param chatId идентификатор чата получателя
     */
    public void sendTyping(SpeechRecognitionBot bot, Long chatId) {
        try {
            bot.execute(new SendChatAction(chatId.toString(), "typing", null));
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки действия", e);
        }
    }

    /**
     * Отправляет Excel файл пользователю в Telegram.
     *
     * @param bot экземпляр бота SpeechRecognitionBot
     * @param chatId идентификатор чата получателя
     * @param excelFile файл в формате Excel для отправки
     */
    public void sendExcel(SpeechRecognitionBot bot, Long chatId, File excelFile) {
        try {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(new InputFile(excelFile, "Статистика игроков.xlsx"));
            sendDocument.setCaption("📊 Статистика игроков в Excel формате");

            bot.execute(sendDocument);

        } catch (TelegramApiException e) {
            log.error("Ошибка отправки Excel файла", e);
            sendText(bot, chatId, "❌ Не удалось отправить Excel файл");
        }
    }

    /**
     * Отправляет сообщение о неподдерживаемом формате аудиофайла.
     *
     * @param bot экземпляр бота SpeechRecognitionBot
     * @param chatId идентификатор чата получателя
     * @param fileName имя файла, который не был обработан
     */
    public void sendUnsupportedFormat(SpeechRecognitionBot bot, Long chatId, String fileName) {
        String message = String.format(UNSUPPORTED_FORMAT_MESSAGE, fileName);
        sendText(bot, chatId, message);
    }

    /**
     * Отправляет приветственное сообщение новому пользователю.
     *
     * @param bot экземпляр бота SpeechRecognitionBot
     * @param chatId идентификатор чата получателя
     * @param userName имя пользователя для персонализации приветствия
     */
    public void sendWelcome(SpeechRecognitionBot bot, Long chatId, String userName) {
        String message = String.format(WELCOME_MESSAGE, userName, COMMAND_HELP, COMMAND_EXAMPLE);
        sendText(bot, chatId, message);
    }

    /**
     * Отправляет справочное сообщение с информацией о работе бота.
     *
     * @param bot экземпляр бота SpeechRecognitionBot
     * @param chatId идентификатор чата получателя
     */
    public void sendHelp(SpeechRecognitionBot bot, Long chatId) {
        String message = String.format(HELP_MESSAGE, COMMAND_START, COMMAND_EXAMPLE);
        sendText(bot, chatId, message);
    }

    /**
     * Отправляет пример использования бота.
     *
     * @param bot экземпляр бота SpeechRecognitionBot
     * @param chatId идентификатор чата получателя
     */
    public void sendExample(SpeechRecognitionBot bot, Long chatId) {
        String message = String.format(EXAMPLE_MESSAGE, COMMAND_START, COMMAND_HELP);
        sendText(bot, chatId, message);
    }

    /**
     * Отправляет сообщение об ошибке пользователю.
     *
     * @param bot экземпляр бота SpeechRecognitionBot
     * @param chatId идентификатор чата получателя
     * @param errorMessage текст ошибки для отображения пользователю
     */
    public void sendError(SpeechRecognitionBot bot, Long chatId, String errorMessage) {
        sendText(bot, chatId, "❌ Ошибка: " + errorMessage);
    }
}