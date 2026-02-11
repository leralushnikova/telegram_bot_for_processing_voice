package com.telegram_bot_for_processing_voice.service.impl;

import com.telegram_bot_for_processing_voice.service.FileService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@DisplayName("Тестирование FileServiceImpl с LocalStack")
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class FileServiceImplTest {

    private static final String BUCKET_NAME = "audio";
    private static final String TEST_MP3_FILE = "test.mp3";
    private static final String TEST_OGG_FILE = "test.ogg";
    private static final String TEST_WAV_FILE = "test.wav";

    @Autowired
    private FileService fileService;

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.8.1"))
            .withServices(S3)
            .withEnv("SERVICES", "s3")
            .withStartupTimeout(java.time.Duration.ofMinutes(2));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("app.bucket", () -> BUCKET_NAME);
        registry.add("spring.cloud.aws.region.static", localStack::getRegion);
        registry.add("spring.cloud.aws.credentials.access-key", localStack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localStack::getSecretKey);
        registry.add("spring.cloud.aws.s3.endpoint", () -> localStack.getEndpointOverride(S3).toString());
    }

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        localStack.execInContainer("awslocal", "s3", "mb", "s3://" + BUCKET_NAME);
    }

    @BeforeEach
    void setUp() {
        cleanBucket();
    }

    @AfterAll
    static void afterAll() {
        localStack.stop();
    }

    private void cleanBucket() {
        try (S3Client s3Client = createS3Client()) {
            s3Client.listObjectsV2(builder -> builder.bucket(BUCKET_NAME))
                    .contents()
                    .forEach(obj -> s3Client.deleteObject(builder -> builder
                            .bucket(BUCKET_NAME)
                            .key(obj.key())
                            .build()));
        }
    }

    private S3Client createS3Client() {
        return S3Client.builder()
                .region(Region.of(localStack.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())))
                .endpointOverride(URI.create(localStack.getEndpointOverride(S3).toString()))
                .build();
    }

    private byte[] createMinimalOggFile() {
        return new byte[]{
                'O', 'g', 'g', 'S', 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        };
    }

    private byte[] createMinimalMp3File() {
        byte[] mp3 = new byte[10];
        mp3[0] = 'I';
        mp3[1] = 'D';
        mp3[2] = '3';
        mp3[3] = 3;
        mp3[4] = 0; // Version
        mp3[6] = 0;
        mp3[7] = 0;
        mp3[8] = 0;
        mp3[9] = 0;
        return mp3;
    }

    @SneakyThrows
    private byte[] createMinimalWavFile() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write("RIFF".getBytes());
        baos.write(intToBytes(36));
        baos.write("WAVE".getBytes());
        baos.write("fmt ".getBytes());
        baos.write(intToBytes(16));
        baos.write(shortToBytes((short) 1));
        baos.write(shortToBytes((short) 1));
        baos.write(intToBytes(16000));
        baos.write(intToBytes(32000));
        baos.write(shortToBytes((short) 2));
        baos.write(shortToBytes((short) 16));
        baos.write("data".getBytes());
        baos.write(intToBytes(0));

        return baos.toByteArray();
    }

    private byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF)
        };
    }

    private byte[] shortToBytes(short value) {
        return new byte[]{
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF)
        };
    }

    private boolean isFfmpegAvailable() {
        try {
            Process process = Runtime.getRuntime().exec("ffmpeg -version");
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("Успешная загрузка OGG файла без конвертации")
    void uploadFileAndGetUri_OggFile_Success() {
        byte[] oggContent = createMinimalOggFile();
        InputStream inputStream = new ByteArrayInputStream(oggContent);

        String result = fileService.uploadFileAndGetUri(inputStream, BUCKET_NAME, TEST_OGG_FILE);

        assertNotNull(result);
        assertTrue(result.contains(BUCKET_NAME));
        assertTrue(result.contains(".ogg"));
        assertTrue(result.contains("telegram/voices/"));
        assertTrue(result.startsWith("http://") || result.startsWith("https://"));
    }

    @Test
    @DisplayName("Успешная загрузка через метод по умолчанию")
    void uploadFileAndGetUri_DefaultMethod_Success() {
        byte[] oggContent = createMinimalOggFile();
        InputStream inputStream = new ByteArrayInputStream(oggContent);

        String result = fileService.uploadFileAndGetUri(inputStream, BUCKET_NAME);

        assertNotNull(result);
        assertTrue(result.contains(".ogg"));
        assertTrue(result.contains("telegram/voices/"));
    }

    @Test
    @DisplayName("Ошибка при конвертации - неподдерживаемый формат")
    void uploadFileAndGetUri_ConversionError_ThrowsException() {
        if (!isFfmpegAvailable()) {
            return;
        }

        byte[] invalidAudio = "not an audio file".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(invalidAudio);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                fileService.uploadFileAndGetUri(inputStream, BUCKET_NAME, TEST_MP3_FILE)
        );

        assertTrue(exception.getMessage().contains("Ошибка при загрузке файла"));
        assertTrue(exception.getCause().getMessage().contains("Ошибка конвертации"));
    }

    @Test
    @DisplayName("Ошибка при загрузке в несуществующий бакет")
    void uploadFileAndGetUri_NonExistentBucket_ThrowsException() {
        byte[] oggContent = createMinimalOggFile();
        InputStream inputStream = new ByteArrayInputStream(oggContent);
        String nonExistentBucket = "non-existent-bucket-" + UUID.randomUUID();

        assertThrows(RuntimeException.class, () ->
                fileService.uploadFileAndGetUri(inputStream, nonExistentBucket, TEST_OGG_FILE)
        );
    }

    @Test
    @DisplayName("Ошибка при загрузке с null InputStream")
    void uploadFileAndGetUri_NullInputStream_ThrowsException() {
        assertThrows(RuntimeException.class, () ->
                fileService.uploadFileAndGetUri(null, BUCKET_NAME, TEST_OGG_FILE)
        );
    }

    @Test
    @DisplayName("Ошибка при загрузке с null bucket")
    void uploadFileAndGetUri_NullBucket_ThrowsException() {
        byte[] oggContent = createMinimalOggFile();
        InputStream inputStream = new ByteArrayInputStream(oggContent);

        assertThrows(RuntimeException.class, () ->
                fileService.uploadFileAndGetUri(inputStream, null, TEST_OGG_FILE)
        );
    }

    @Test
    @DisplayName("Загрузка файла с неизвестным расширением - попытка конвертации")
    void uploadFileAndGetUri_UnknownExtension_RequiresConversion() {
        if (!isFfmpegAvailable()) {
            return;
        }

        byte[] audioContent = createMinimalMp3File();
        InputStream inputStream = new ByteArrayInputStream(audioContent);
        String unknownFile = "test.unknown";

        assertThrows(RuntimeException.class, () ->
                fileService.uploadFileAndGetUri(inputStream, BUCKET_NAME, unknownFile)
        );
    }

    @Test
    @DisplayName("Загрузка WAV файла с конвертацией")
    void uploadFileAndGetUri_WavFileWithConversion_Success() {
        if (!isFfmpegAvailable()) {
            return;
        }

        byte[] wavContent = createMinimalWavFile();
        InputStream inputStream = new ByteArrayInputStream(wavContent);

        String result = fileService.uploadFileAndGetUri(inputStream, BUCKET_NAME, TEST_WAV_FILE);

        assertNotNull(result);
        assertTrue(result.contains(BUCKET_NAME));
        assertTrue(result.contains(".ogg"));
        assertTrue(result.contains("telegram/voices/"));
    }
}