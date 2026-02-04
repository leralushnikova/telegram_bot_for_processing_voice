package com.telegram_bot_for_processing_voice.service.impl.token;

import com.telegram_bot_for_processing_voice.dto.JwtTokenDTO;
import com.telegram_bot_for_processing_voice.property.JwtProperties;
import com.telegram_bot_for_processing_voice.service.JwtService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Date;

import static com.telegram_bot_for_processing_voice.util.Constants.EXPIRES_IN_TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    static {
        Security.addProvider(new BouncyCastleProvider());
        log.debug("BouncyCastleProvider зарегистрирован как провайдер безопасности");
    }

    @Value("${yandex.iam.url}")
    private String url;

    private final JwtProperties jwtProperties;

    @Override
    public JwtTokenDTO getJwtToken() {
        log.info("Начало генерации JWT токена для Yandex Cloud IAM");

        PemObject privateKeyPem = parsePrivateKeyFromPem();

        PrivateKey privateKey = createPrivateKeyFromPem(privateKeyPem);

        Instant now = Instant.now();
        String token =  Jwts.builder()
                .setHeaderParam("kid", jwtProperties.getId())
                .setIssuer(jwtProperties.getServiceAccountId())
                .setAudience(url)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(EXPIRES_IN_TOKEN)))
                .signWith(privateKey, SignatureAlgorithm.PS256)
                .compact();

        return new JwtTokenDTO(token);
    }

    /**
     * Парсит приватный ключ из PEM формата.
     * @return возвращает объект PemObject
     */
    private PemObject parsePrivateKeyFromPem() {
        PemObject privateKeyPem;
        try (PemReader reader = new PemReader(new StringReader(jwtProperties.getPrivateKey()))) {
            privateKeyPem = reader.readPemObject();
            if (privateKeyPem == null) {
                throw new IllegalArgumentException("Не удалось прочитать приватный ключ из PEM");
            }
        } catch (IOException e) {
            log.error("Ошибка чтения приватного ключа", e);
            throw new JwtException("Не удалось прочитать приватный ключ", e);

        }

        return privateKeyPem;
    }

    /**
     * Создает фабрику ключей для указанного алгоритма шифрования.
     *
     * @return {@link KeyFactory} для работы с RSA ключами
     * @throws JwtException если алгоритм RSA не поддерживается в текущей среде выполнения
     */
    private KeyFactory getKeyFactory() {
        String algorithmEncoding = "RSA";
        try {
            return KeyFactory.getInstance(algorithmEncoding);
        } catch (NoSuchAlgorithmException e) {
            log.error("Алгоритм шифрования {} не поддерживается в текущей среде выполнения Java", algorithmEncoding, e);
            throw new JwtException(String.format(
                    "Алгоритм %s не поддерживается. Требуется JRE с поддержкой RSA шифрования", algorithmEncoding), e);
        }
    }

    /**
     * Создает объект приватного ключа из данных PEM объекта.
     *
     * @param privateKeyPem PEM объект, содержащий данные приватного ключа
     * @return {@link PrivateKey} объект приватного ключа
     * @throws JwtException если формат ключа не соответствует спецификации PKCS#8
     */
    private PrivateKey createPrivateKeyFromPem(PemObject privateKeyPem) {
        try {
            return getKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(privateKeyPem.getContent()));
        } catch (InvalidKeySpecException e) {
            log.error("Некорректная спецификация приватного ключа.", e);
            throw new JwtException("Приватный ключ имеет некорректный формат.", e);
        }
    }

}
