package com.telegram_bot_for_processing_voice.handler;

import com.telegram_bot_for_processing_voice.bot.SpeechRecognitionBot;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Интерфейс для обработчиков сообщений в Telegram боте.
 */
public interface MessageHandler {

    /**
     * Определяет, может ли данный обработчик обработать полученное сообщение.
     *
     * @param message объект входящего сообщения от Telegram API
     * @return возвращает true если обработчик может обработать данное сообщение.
     */
    boolean canHandle(Message message);

    /**
     * Выполняет обработку сообщения и отправку ответа пользователю.
     *
     * @param message объект входящего сообщения от Telegram API
     * @param bot экземпляр бота SpeechRecognitionBot
     */
    void handle(Message message, SpeechRecognitionBot bot);
}