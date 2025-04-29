package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class MinioFileStorageTest {

    private static final String BUCKET = "user-files";

    @Container
    private static final MinIOContainer MINIO_CONTAINER = new MinIOContainer("minio/minio")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "user")
            .withEnv("MINIO_ROOT_PASSWORD", "password")
            .withCommand("server /data");

    @TestConfiguration
    public static class MinioConfig {
        @Bean
        public MinioClient minioClient() {
            try {
                String minioEndpoint = "http://"
                        + MINIO_CONTAINER.getHost()
                        + ":"
                        + MINIO_CONTAINER.getMappedPort(9000);

                MinioClient minioClient =
                        MinioClient.builder()
                                .endpoint(minioEndpoint)
                                .credentials("user", "password")
                                .build();
                boolean found =
                        minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build());
                if (!found) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
                }
                return minioClient;
            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @BeforeEach
    void cleanUp() throws Exception {
        boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
        }

        Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(BUCKET)
                .recursive(true)
                .build());

        for (Result<Item> object : objects) {
            String objectName = object.get().objectName();
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(BUCKET).object(objectName).build());
        }
    }

    @Autowired
    private MinioClient minioClient;


    @Autowired
    private MinioFileStorage fileStorage;

    @Test
    void createPath() {
        fileStorage.createPath(1L, "folder/");
        StorageObjectInfo expectedObject = new StorageObjectInfo.Builder("folder/")
                .objectSize(0)
                .build();

        List<StorageObjectInfo> actualObjects = fileStorage.listObjectInfo(1L, "").get();

        assertThat(actualObjects)
                .as("Count of objects")
                .hasSize(1)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(expectedObject);
    }

    @ParameterizedTest
    @ValueSource(strings = {"..", "^", "*", "|", "\\", "&", "\"", ";"})
    void createNotValidPath(String notValidPath) {
        assertThrows(IllegalArgumentException.class, () ->
                fileStorage.createPath(1L, notValidPath)
        );
    }

    @Test
    void createVeryLongPath() {
        String veryLongPath = "a".repeat(1025);
        assertThrows(IllegalArgumentException.class, () ->
                fileStorage.createPath(1L, veryLongPath)
        );
    }

    @Test
    void saveAndGetObject() throws IOException {
        MultipartFile file = new MockMultipartFile("file.txt", "file.txt", "text/plain", "Hello".getBytes());

        fileStorage.saveObject(1L, "folder/", file);

        FileObject actual = fileStorage.getObject(1L, "folder/file.txt");

        try (InputStream actualStream = actual.stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            assertThat(actualBytes)
                    .as("Match file content")
                    .isEqualTo(file.getBytes());
        }

        assertThat(actual.path())
                .as("Match path")
                .isEqualTo("folder/file.txt");
    }

    @Test
    void saveAndDeleteObjects() {
        MultipartFile file = new MockMultipartFile("file.txt", "file.txt", "text/plain", "Hello".getBytes());
        fileStorage.saveObject(1L, "folder/subFolder1", file);
        fileStorage.saveObject(1L, "folder/subFolder2", file);
        assertThat(fileStorage.listObjectInfo(1L, "folder/").get())
                .hasSize(2);

        fileStorage.deleteObjects(1L, "folder/");

        assertThat(fileStorage.listObjectInfo(1L, "").get())
                .hasSize(0);
    }

    @Test
    void getStorageObjectsByNotExistPath() {
        assertFalse(fileStorage.listObjectInfo(1L, "not-exist-path").isPresent());
    }

    @Test
    void getStorageObjectsByRoot() {
        assertTrue(fileStorage.listObjectInfo(1L, "").isPresent());
    }

    @Test
    void shouldNotDeleteRootFolder() {
        fileStorage.deleteObjects(1L, "");
        assertTrue(fileStorage.listObjectInfo(1L, "").isPresent());
    }

    @Test
    void copyObject() throws IOException {
        MultipartFile file = new MockMultipartFile("file.txt", "file.txt", "text/plain", "Hello".getBytes());
        fileStorage.saveObject(1L, "folder/", file);

        fileStorage.copyObject(1L, "folder/file.txt", "folder/copy-file.txt");

        FileObject actual = fileStorage.getObject(1L, "folder/copy-file.txt");

        try (InputStream actualStream = actual.stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            assertThat(actualBytes)
                    .as("Match file content")
                    .isEqualTo(file.getBytes());
        }

        assertThat(actual.path())
                .as("Match path")
                .isEqualTo("folder/copy-file.txt");
    }
}
