package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.exception.AudioException;
import com.telegram_bot_for_processing_voice.service.AudioConverterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static com.telegram_bot_for_processing_voice.util.Constants.BUFFER_SIZE;

@Slf4j
@Service
public class FfmpegAudioConverterService implements AudioConverterService {
    
    @Value("${audio.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;
    
    @Override
    public InputStream convertToOgg(InputStream inputStream, String originalFileName) throws IOException {
        File inputFile = File.createTempFile("input_", getExtension(originalFileName));
        File outputFile = File.createTempFile("output_", ".ogg");
        
        try {
            try (FileOutputStream fos = new FileOutputStream(inputFile)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            
            convertToOggWithFfmpeg(inputFile, outputFile);
            
            return new DeleteOnCloseFileInputStream(outputFile) {
                @Override
                public void close() throws IOException {
                    super.close();
                    inputFile.delete();
                }
            };
            
        } catch (Exception e) {
            inputFile.delete();
            outputFile.delete();
            throw new AudioException("Ошибка конвертации аудио: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean needsConversion(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return true;
        }

        String ext = getExtension(fileName).toLowerCase();
        return !"ogg".equals(ext) && !"oga".equals(ext) && !"opus".equals(ext);
    }
    
    private void convertToOggWithFfmpeg(File inputFile, File outputFile) throws IOException, InterruptedException {
        List<String> command = Arrays.asList(
            ffmpegPath,
            "-i", inputFile.getAbsolutePath(),
            "-c:a", "libopus",
            "-b:a", "16k",
            "-ar", "16000",
            "-ac", "1",
            "-vn",
            "-y",
            outputFile.getAbsolutePath()
        );
        
        log.debug("Конвертируем аудио: {}", String.join(" ", command));
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.trace("ffmpeg: {}", line);
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("ffmpeg output: {}", output);
            throw new AudioException("Ошибка конвертации ffmpeg, код: " + exitCode);
        }
        
        log.info("Конвертация завершена: {} -> {}", 
                inputFile.getName(), outputFile.getName());
    }
    
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    
    /**
     * InputStream, который удаляет файл при закрытии.
     */
    private static class DeleteOnCloseFileInputStream extends FileInputStream {
        private final File file;
        
        DeleteOnCloseFileInputStream(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
        }
        
        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }
}