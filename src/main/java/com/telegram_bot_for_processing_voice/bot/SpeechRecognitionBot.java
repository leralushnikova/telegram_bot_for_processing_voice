package com.telegram_bot_for_processing_voice.bot;

import com.telegram_bot_for_processing_voice.model.enums.Action;
import com.telegram_bot_for_processing_voice.service.ExcelStatsService;
import com.telegram_bot_for_processing_voice.service.FileService;
import com.telegram_bot_for_processing_voice.service.SpeechRecognitionService;
import com.telegram_bot_for_processing_voice.service.StatAnalyzeService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Audio;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.telegram_bot_for_processing_voice.util.Constants.COMMAND_EXAMPLE;
import static com.telegram_bot_for_processing_voice.util.Constants.COMMAND_HELP;
import static com.telegram_bot_for_processing_voice.util.Constants.COMMAND_START;
import static com.telegram_bot_for_processing_voice.util.Constants.EXAMPLE_MESSAGE;
import static com.telegram_bot_for_processing_voice.util.Constants.HELP_MESSAGE;
import static com.telegram_bot_for_processing_voice.util.Constants.SUPPORTED_AUDIO_EXTENSIONS;
import static com.telegram_bot_for_processing_voice.util.Constants.SUPPORTED_MIME_TYPES;
import static com.telegram_bot_for_processing_voice.util.Constants.TEXT_MESSAGE;
import static com.telegram_bot_for_processing_voice.util.Constants.UNSUPPORTED_FORMAT_MESSAGE;
import static com.telegram_bot_for_processing_voice.util.Constants.WELCOME_MESSAGE;

/**
 * Telegram бот для распознавания речи и анализа спортивной статистики.
 */
@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class SpeechRecognitionBot extends TelegramLongPollingBot {

    private static final long MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024;
    private static final int MAX_DURATION_SECONDS = 45 * 60;
    private static final int CONNECT_TIMEOUT_MS = 30_000;
    private static final int READ_TIMEOUT_MS = 60_000;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${yandex.storage.bucket}")
    private String bucket;

    private final SpeechRecognitionService speechService;
    private final StatAnalyzeService statAnalyzeService;
    private final ExcelStatsService excelStatsService;
    private final FileService fileService;

    /**
     * Инициализирует бота после создания бина.
     */
    @PostConstruct
    public void init() {
        registerBotCommands();
    }

    /**
     * Регистрирует команды бота в Telegram.
     */
    private void registerBotCommands() {
        try {
            List<BotCommand> commands = new ArrayList<>();
            commands.add(new BotCommand(COMMAND_START, "Начать работу с ботом"));
            commands.add(new BotCommand(COMMAND_HELP, "Помощь и инструкции"));
            commands.add(new BotCommand(COMMAND_EXAMPLE, "Пример использования"));

            execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
            log.info("Команды бота успешно зарегистрированы в Telegram");

        } catch (TelegramApiException e) {
            log.error("Не удалось зарегистрировать команды бота", e);
        }
    }

    /**
     * Обрабатывает входящие обновления от Telegram.
     *
     * @param update объект обновления от Telegram API
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        Message message = update.getMessage();
        Long chatId = message.getChatId();

        try {
            boolean messageHandled = false;

            if (message.hasText() && message.getText().startsWith("/")) {
                handleCommand(message);
                messageHandled = true;
            } else if (message.hasText()) {
                handleTextMessage(message);
                messageHandled = true;
            } else if (message.hasVoice()) {
                handleVoiceMessage(message);
                messageHandled = true;
            } else if (message.hasAudio()) {
                handleAudioFileMessage(message);
                messageHandled = true;
            } else if (message.hasDocument()) {
                handleDocumentMessage(message);
                messageHandled = true;
            }

            if (!messageHandled) {
                sendTextMessage(chatId, TEXT_MESSAGE);
            }

        } catch (Exception e) {
            log.error("Ошибка обработки сообщения", e);
            sendTextMessage(chatId, "❌ Ошибка: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает голосовое сообщение от пользователя.
     *
     * @param message объект сообщения Telegram с голосовым сообщением
     */
    private void handleVoiceMessage(Message message) {
        Long chatId = message.getChatId();
        Voice voice = message.getVoice();

        try {
            sendTypingAction(chatId);
            sendTextMessage(chatId, "🎤 Скачиваю голосовое сообщение...");

            InputStream inputStream = downloadVoiceMessageAsStream(voice.getFileId());

            String uri = fileService.uploadFileAndGetUri(inputStream, bucket);

            sendTextMessage(chatId, "🔍 Распознаю речь...");

            int audioDuration = voice.getDuration();
            String recognizedText = speechService.getTextFromVoice(uri, audioDuration);

            handleTextToMap(chatId, recognizedText);

        } catch (Exception e) {
            log.error("Ошибка обработки голосового сообщения", e);
            sendTextMessage(chatId, "❌ Ошибка при обработке голосового сообщения: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает аудио файл от пользователя.
     *
     * @param message объект сообщения Telegram с голосовым сообщением
     */
    private void handleAudioFileMessage(Message message) {
        Long chatId = message.getChatId();
        Audio audio = message.getAudio();

        try {
            sendTypingAction(chatId);
            sendTextMessage(chatId, "🎵 Скачиваю аудиофайл...");

            String fileName = audio.getFileName();
            String mimeType = audio.getMimeType();
            Integer duration = audio.getDuration();

            log.info("Получен аудиофайл: {} (MIME: {}, длительность: {} сек)",
                    fileName, mimeType, duration);

            if (!isSupportedAudioFormat(fileName, mimeType)) {
                sendUnsupportedFormatMessage(chatId, fileName);
                return;
            }

            Long fileSize = audio.getFileSize();
            if (fileSize != null && fileSize > MAX_FILE_SIZE_BYTES) {
                sendTextMessage(chatId,
                        String.format("❌ Файл слишком большой (%.1f MB). Максимальный размер: 50 MB",
                                fileSize / (1024.0 * 1024.0)));
                return;
            }

            if (duration != null && duration > MAX_DURATION_SECONDS) {
                sendTextMessage(chatId,
                        String.format("❌ Аудио слишком длинное (%d минут). Максимальная длительность: 40 минут",
                                duration / 60));
                return;
            }

            InputStream inputStream = downloadFileAudioAsStream(audio.getFileId());

            sendTextMessage(chatId, "🔄 Конвертирую в формат для распознавания...");

            String uri = fileService.uploadFileAndGetUri(inputStream, bucket, fileName);

            sendTextMessage(chatId, "🔍 Распознаю речь...");

            String recognizedText = speechService.getTextFromVoice(uri, duration);
            handleTextToMap(chatId, recognizedText);

        } catch (Exception e) {
            log.error("Ошибка обработки аудиофайла", e);
            sendTextMessage(chatId, "❌ Ошибка при обработке аудиофайла: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает документ (возможно, аудио файл).
     *
     * @param message объект сообщения Telegram с голосовым сообщением
     */
    private void handleDocumentMessage(Message message) {
        Long chatId = message.getChatId();
        Document document = message.getDocument();

        try {
            sendTypingAction(chatId);
            sendTextMessage(chatId, "📄 Проверяю документ...");

            String fileName = document.getFileName();
            String mimeType = document.getMimeType();
            Long fileSize = document.getFileSize();

            log.info("Получен документ: {} (MIME: {}, размер: {} bytes)",
                    fileName, mimeType, fileSize);

            if (!isSupportedAudioFormat(fileName, mimeType)) {
                sendTextMessage(chatId,
                        "❌ Это не аудиофайл. Пожалуйста, отправьте MP3, WAV, FLAC или другой аудиофайл.");
                return;
            }

            if (fileSize != null && fileSize > MAX_FILE_SIZE_BYTES) {
                sendTextMessage(chatId,
                        String.format("❌ Файл слишком большой (%.1f MB). Максимальный размер: 50 MB",
                                fileSize / (1024.0 * 1024.0)));
                return;
            }

            InputStream inputStream = downloadFileAsStream(document.getFileId());

            sendTextMessage(chatId, "🔄 Обрабатываю аудиофайл...");

            String uri = fileService.uploadFileAndGetUri(inputStream, bucket, fileName);

            sendTextMessage(chatId, "🔍 Распознаю речь...");

            String recognizedText = speechService.getTextFromVoice(uri, null);
            handleTextToMap(chatId, recognizedText);

        } catch (Exception e) {
            log.error("Ошибка обработки документа", e);
            sendTextMessage(chatId, "❌ Ошибка при обработке документа: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает сообщение от пользователя.
     *
     * @param message объект сообщения Telegram с голосовым сообщением
     */
    private void handleTextMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();

        try {
            sendTypingAction(chatId);

            sendTextMessage(chatId, "Обрабатываю сообщение...");

            handleTextToMap(chatId, text);

        } catch (Exception e) {
            log.error("Ошибка обработки голосового сообщения", e);
            sendTextMessage(chatId, "❌ Ошибка при обработке голосового сообщения: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает сообщение и отправляет Excel файл пользователю.
     *
     * @param text сообщения Telegram от пользователя.
     */
    private void handleTextToMap(Long chatId, String text) throws IOException {

        log.debug(text);

        Map<String, Map<Action, Integer>> stats = statAnalyzeService.parseGameText(text.toLowerCase());

        if (stats.isEmpty()) {
            sendTextMessage(chatId, "⚠️ Не удалось извлечь статистику из текста.");
        } else {
            File excelFile = excelStatsService.createExcelStatsFile(stats);

            sendExcelFile(chatId, excelFile);

            excelFile.delete();
        }
    }

    /**
     * Отправляет Excel файл пользователю.
     *
     * @param chatId    идентификатор чата в Telegram
     * @param excelFile файл excel
     */
    private void sendExcelFile(Long chatId, File excelFile) {
        try {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId.toString());
            sendDocument.setDocument(new InputFile(excelFile, "Статистика игроков.xlsx"));
            sendDocument.setCaption("📊 Статистика игроков в Excel формате");

            execute(sendDocument);

        } catch (TelegramApiException e) {
            log.error("Ошибка отправки Excel файла", e);
            sendTextMessage(chatId, "❌ Не удалось отправить Excel файл");
        }
    }

    /**
     * Преобразует голосовое сообщение в поток данных.
     *
     * @param fileId идентификатор файла
     * @return поток данных.
     * @throws IOException если произошла ошибка при скачивании файла
     * @throws TelegramApiException если произошла ошибка при скачивании файла
     */
    private InputStream downloadVoiceMessageAsStream(String fileId) throws IOException, TelegramApiException {
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);
        org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);

        String fileUrl = file.getFileUrl(getBotToken());

        URL url = new URL(fileUrl);
        URLConnection connection = url.openConnection();
        return connection.getInputStream();
    }

    /**
     * Преобразует аудио сообщение в поток данных.
     *
     * @param fileId идентификатор файла
     * @return поток данных.
     * @throws IOException если произошла ошибка при скачивании файла
     * @throws TelegramApiException если произошла ошибка при скачивании файла
     */
    private InputStream downloadFileAudioAsStream(String fileId) throws IOException, TelegramApiException {
        GetFile getFile = new GetFile();
        getFile.setFileId(fileId);
        org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);

        String fileUrl = file.getFileUrl(getBotToken());
        log.debug("Скачиваю файл: {}", fileUrl);

        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);

        return connection.getInputStream();
    }

    /**
     * Обрабатывает текстовые команды от пользователя.
     *
     * @param message объект сообщения Telegram с командой
     */
    private void handleCommand(Message message) {
        Long chatId = message.getChatId();
        String command = message.getText();

        switch (command) {
            case COMMAND_START:
                sendWelcomeMessage(chatId, message.getFrom().getFirstName());
                break;

            case COMMAND_HELP:
                sendHelpMessage(chatId);
                break;

            case COMMAND_EXAMPLE:
                sendExampleMessage(chatId);
                break;

            default:
                sendTextMessage(chatId, "Неизвестная команда. Используйте " + COMMAND_HELP + "  для списка команд.");
        }
    }

    /**
     * Проверяет, поддерживается ли аудиоформат.
     *
     * @param fileName имя файла
     * @param mimeType тип файла
     * @return true, если такое расширение файла поддерживается
     */
    private boolean isSupportedAudioFormat(String fileName, String mimeType) {
        if (fileName != null) {
            String extension = getFileExtension(fileName).toLowerCase();
            if (SUPPORTED_AUDIO_EXTENSIONS.contains(extension)) {
                return true;
            }
        }

        return mimeType != null && SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase());
    }

    /**
     * Извлекает расширение файла.
     *
     * @param fileName имя файла
     * @return возвращает расширение файла
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Отправляет сообщение о неподдерживаемом формате.
     *
     * @param chatId   идентификатор чата в Telegram
     * @param fileName имя файла
     */
    private void sendUnsupportedFormatMessage(Long chatId, String fileName) {
        String unsupportedFormatMessage  = String.format(UNSUPPORTED_FORMAT_MESSAGE, fileName);
        sendTextMessage(chatId, unsupportedFormatMessage);
    }

    /**
     * Отправляет приветственное сообщение новому пользователю.
     *
     * @param chatId   идентификатор чата в Telegram
     * @param userName имя пользователя, полученное из Telegram
     */
    private void sendWelcomeMessage(Long chatId, String userName) {
        String welcomeMessage = String.format(WELCOME_MESSAGE, userName, COMMAND_HELP, COMMAND_EXAMPLE);

        sendTextMessage(chatId, welcomeMessage);
    }

    /**
     * Отправляет справку по использованию бота.
     *
     * @param chatId идентификатор чата в Telegram
     */
    private void sendHelpMessage(Long chatId) {
        String helpMessage = String.format(HELP_MESSAGE, COMMAND_START, COMMAND_EXAMPLE);

        sendTextMessage(chatId, helpMessage);
    }

    /**
     * Отправляет пример по использованию бота.
     *
     * @param chatId идентификатор чата в Telegram
     */
    private void sendExampleMessage(Long chatId) {
        String helpMessage = String.format(EXAMPLE_MESSAGE, COMMAND_START, COMMAND_HELP);

        sendTextMessage(chatId, helpMessage);
    }

    /**
     * Отправляет текстовое сообщение пользователю.
     *
     * @param chatId идентификатор чата в Telegram
     * @param text   текст сообщения для отправки
     */
    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

    /**
     * Отправляет индикатор "печатает" (typing indicator).
     *
     * @param chatId идентификатор чата в Telegram
     */
    private void sendTypingAction(Long chatId) {
        try {
            execute(new SendChatAction(
                    chatId.toString(), "typing", null
            ));
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки действия", e);
        }
    }
}