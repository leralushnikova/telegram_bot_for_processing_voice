package com.telegram_bot_for_processing_voice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * The {@code Application} class is the entry point for the application.
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.telegram_bot_for_processing_voice.feign")
public class TelegramBotForProcessingVoiceApplication {

    /**
     * The main method serves as the entry point for the application.
     *
     * @param args The arguments of the main method.
     */
    public static void main(String[] args) {
        SpringApplication.run(TelegramBotForProcessingVoiceApplication.class, args);
    }

}
