package com.telegram_bot_for_processing_voice.service.impl.token;

import com.telegram_bot_for_processing_voice.dto.JwtTokenDTO;
import com.telegram_bot_for_processing_voice.property.JwtProperties;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@EnableConfigurationProperties(JwtProperties.class)
@SpringBootTest(classes = {JwtServiceImpl.class, JwtProperties.class})
@DisplayName("Тестирование методов сервиса JwtServiceImpl.")
class JwtServiceImplTest {

    @Autowired
    JwtServiceImpl jwtService;

    String url = Instancio.create(String.class);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "url", url);
    }

    @Test
    @DisplayName("Проверка генерации токена")
    void getJwtToken() {

        JwtTokenDTO jwtTokenDTO = jwtService.getJwtToken();

        assertNotNull(jwtTokenDTO);
    }
}