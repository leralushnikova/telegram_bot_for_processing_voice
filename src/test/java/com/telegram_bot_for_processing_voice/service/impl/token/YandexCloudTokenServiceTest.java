package com.telegram_bot_for_processing_voice.service.impl.token;

import com.telegram_bot_for_processing_voice.dto.JwtTokenDTO;
import com.telegram_bot_for_processing_voice.dto.YandexCloudTokenDTO;
import com.telegram_bot_for_processing_voice.feign.YandexCloudTokenClient;
import com.telegram_bot_for_processing_voice.service.JwtService;
import com.telegram_bot_for_processing_voice.service.conf.TestYandexCloudConfig;
import org.assertj.core.api.SoftAssertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {YandexCloudTokenService.class, TestYandexCloudConfig.class})
@DisplayName("Тестирование методов сервиса YandexCloudTokenService.")
class YandexCloudTokenServiceTest {

    @Autowired
    private YandexCloudTokenService yandexCloudTokenService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private YandexCloudTokenClient tokensClient;

    @MockitoBean
    private JwtService jwtService;

    private YandexCloudTokenDTO token;

    private Cache cache;

    String accessToken = "accessToken";
    String yandexCloudToken = "yandexCloudToken";

    @BeforeEach
    void setUp() {
        token = new YandexCloudTokenDTO(accessToken);
        cache = cacheManager.getCache(yandexCloudToken);
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    @DisplayName("Должен сохранить JWT-токен в кэше при первом запросе")
    void getJwtTokenSuccess() {
        String userId = "user123";
        ResponseEntity<YandexCloudTokenDTO> response = new ResponseEntity<>(token, HttpStatus.OK);
        JwtTokenDTO jwtTokenDTO = Instancio.create(JwtTokenDTO.class);

        when(jwtService.getJwtToken()).thenReturn(jwtTokenDTO);
        when(tokensClient.generateToken(jwtTokenDTO)).thenReturn(response);

        YandexCloudTokenDTO result = yandexCloudTokenService.getIamToken(userId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).isNotNull();
            softly.assertThat(result.iamToken()).isEqualTo(accessToken);
            softly.assertThat(cache).isNotNull();
            YandexCloudTokenDTO cachedToken = cache.get(userId, YandexCloudTokenDTO.class);
            softly.assertThat(cachedToken).isNotNull();
            softly.assertThat(cachedToken.iamToken()).isEqualTo(accessToken);
        });

        verify(jwtService).getJwtToken();
        verify(tokensClient).generateToken(jwtTokenDTO);
    }

    @Test
    @DisplayName("Не должен сохранять JWT-токен в кэше при ошибке")
    void getJwtTokenFailed() {
        String userId = "user123";
        JwtTokenDTO jwtTokenDTO = Instancio.create(JwtTokenDTO.class);

        when(jwtService.getJwtToken()).thenReturn(jwtTokenDTO);
        when(tokensClient.generateToken(jwtTokenDTO)).thenThrow(new RuntimeException("Ошибка генерации токена"));

        assertThrows(
                RuntimeException.class,
                () -> yandexCloudTokenService.getIamToken(userId),
                "Ошибка генерации токена"
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(cache).isNotNull();

            if (cache != null) {
                YandexCloudTokenDTO cachedToken = cache.get(userId, YandexCloudTokenDTO.class);
                softly.assertThat(cachedToken).isNull();
            }
        });

        verify(tokensClient).generateToken(jwtTokenDTO);
    }
}