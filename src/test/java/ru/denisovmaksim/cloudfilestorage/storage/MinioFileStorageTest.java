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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class MinioFileStorageTest {

    private static final Long USER_ID = 1L;
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
                String minioEndpoint = "http://" + MINIO_CONTAINER.getHost() + ":"
                        + MINIO_CONTAINER.getMappedPort(9000);

                MinioClient minioClient = MinioClient.builder()
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
        fileStorage.createPath(USER_ID, "folder/");

        StorageObjectInfo expectedObject = new StorageObjectInfo("folder/", "folder", true, 0);

        List<StorageObjectInfo> actualObjects = fileStorage.listObjectInfo(USER_ID, "").get();

        assertThat(actualObjects)
                .as("Count of objects")
                .hasSize(1)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(expectedObject);
    }

    @ParameterizedTest
    @ValueSource(strings = {"..", "*", "|", "\\", "\"", ";"})
    void createNotValidPath(String notValidPath) {
        assertThrows(IllegalArgumentException.class, () ->
                fileStorage.createPath(USER_ID, notValidPath)
        );
    }

    @Test
    void createVeryLongPath() {
        String veryLongPath = "a".repeat(1025);
        assertThrows(IllegalArgumentException.class, () ->
                fileStorage.createPath(USER_ID, veryLongPath)
        );
    }

    @Test
    void saveAndGetObject() throws IOException {
        MultipartFile file = new MockMultipartFile("file.txt", "file.txt",
                "text/plain", "Hello".getBytes());

        fileStorage.saveObject(USER_ID, "folder/", file);

        FileObject actual = fileStorage.getObject(USER_ID, "folder/file.txt");

        try (InputStream actualStream = actual.stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            assertThat(actualBytes)
                    .as("Match file content")
                    .isEqualTo(file.getBytes());
        }

        assertTrue(fileStorage.isExist(USER_ID, "folder/"));
        assertTrue(fileStorage.isExist(USER_ID, "folder/file.txt"));
        assertThat(actual.path())
                .as("Match path")
                .isEqualTo("folder/file.txt");
    }

    @Test
    void saveAndDeleteObjects() {
        MultipartFile file = new MockMultipartFile("file.txt", "file.txt", "text/plain", "Hello".getBytes());
        fileStorage.saveObject(USER_ID, "folder/subFolder1", file);
        fileStorage.saveObject(USER_ID, "folder/subFolder2", file);
        assertThat(fileStorage.listObjectInfo(USER_ID, "folder/").get())
                .hasSize(2);

        fileStorage.deleteObjects(USER_ID, "folder/");
        Optional<List<StorageObjectInfo>> infos = fileStorage.listObjectInfo(USER_ID, "");
        assertThat(infos).isPresent();
        assertThat(infos.get()).isEmpty();
    }

    @Test
    void getManyObjects() throws IOException {
        MultipartFile firstFile = new MockMultipartFile("firstFile.txt", "firstFile.txt",
                "text/plain", "First".getBytes());
        MultipartFile secondFile = new MockMultipartFile("secondFile.txt", "secondFile.txt",
                "text/plain", "Second".getBytes());
        fileStorage.saveObject(USER_ID, "folder/", firstFile);
        fileStorage.saveObject(USER_ID, "folder/", secondFile);

        List<FileObject> actual = fileStorage.getObjects(USER_ID, "folder/");

        assertThat(actual).hasSize(2);

        try (InputStream actualStream = actual.get(0).stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            assertThat(actualBytes)
                    .as("Match first file content")
                    .isEqualTo(firstFile.getBytes());
        }

        try (InputStream actualStream = actual.get(1).stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            assertThat(actualBytes)
                    .as("Match second file content")
                    .isEqualTo(secondFile.getBytes());
        }
    }

    @Test
    void copyManyObjects() throws IOException {
        MultipartFile firstFile = new MockMultipartFile("firstFile.txt", "firstFile.txt",
                "text/plain", "First".getBytes());
        MultipartFile secondFile = new MockMultipartFile("secondFile.txt", "secondFile.txt",
                "text/plain", "Second".getBytes());
        fileStorage.saveObject(USER_ID, "root/folder/", firstFile);
        fileStorage.saveObject(USER_ID, "root/folder/", secondFile);

        fileStorage.copyObjects(USER_ID, "root/folder/", "root/copiedFolder/");

        List<FileObject> actual = fileStorage.getObjects(USER_ID, "root/copiedFolder/");

        assertThat(actual).hasSize(2);

        try (InputStream actualStream = actual.get(0).stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            assertThat(actualBytes)
                    .as("Match first file content")
                    .isEqualTo(firstFile.getBytes());
        }

        try (InputStream actualStream = actual.get(1).stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            assertThat(actualBytes)
                    .as("Match second file content")
                    .isEqualTo(secondFile.getBytes());
        }
    }

    @Test
    void getStorageObjectsByNotExistPath() {
        assertFalse(fileStorage.isExist(USER_ID, "not-exist-path"));
        assertFalse(fileStorage.listObjectInfo(USER_ID, "not-exist-path").isPresent());
    }

    @Test
    void getStorageObjectsByRoot() {
        assertTrue(fileStorage.listObjectInfo(USER_ID, "").isPresent());
    }

    @Test
    void shouldNotDeleteRootFolder() {
        fileStorage.deleteObjects(USER_ID, "");
        assertTrue(fileStorage.listObjectInfo(USER_ID, "").isPresent());
    }

    @Test
    void copyObject() throws IOException {
        MultipartFile file = new MockMultipartFile("file.txt", "file.txt", "text/plain", "Hello".getBytes());
        fileStorage.saveObject(USER_ID, "folder/", file);

        fileStorage.copyOneObject(USER_ID, "folder/file.txt", "folder/copy-file.txt");

        FileObject actual = fileStorage.getObject(USER_ID, "folder/copy-file.txt");

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

    @Test
    void shouldReturnDirectChildCount() {
        MultipartFile firstFile = new MockMultipartFile("firstFile.txt", "firstFile.txt",
                "text/plain", "First".getBytes());
        MultipartFile secondFile = new MockMultipartFile("secondFile.txt", "secondFile.txt",
                "text/plain", "Second".getBytes());
        MultipartFile rootFile = new MockMultipartFile("rootFile.txt", "rootFile.txt",
                "text/plain", "Root".getBytes());

        fileStorage.saveObject(USER_ID, "", rootFile);
        fileStorage.saveObject(USER_ID, "folder/", firstFile);
        fileStorage.saveObject(USER_ID, "folder/", secondFile);
        fileStorage.createPath(USER_ID, "emptyFolder/");

        Long rootCount = fileStorage.getDirectChildCount(USER_ID, "");
        Long folderCount = fileStorage.getDirectChildCount(USER_ID, "folder/");
        Long emptyCount = fileStorage.getDirectChildCount(USER_ID, "emptyFolder/");

        assertThat(rootCount).isEqualTo(3L);
        assertThat(folderCount).isEqualTo(2L);
        assertThat(emptyCount).isZero();
    }

    @Test
    void shouldSearchObjects() {
        MultipartFile firstFile = new MockMultipartFile("firstFile.txt", "firstFile.txt",
                "text/plain", "First".getBytes());
        MultipartFile secondFile = new MockMultipartFile("secondFile.txt", "secondFile.txt",
                "text/plain", "Second".getBytes());
        MultipartFile rootFile = new MockMultipartFile("root.txt", "root.txt",
                "text/plain", "Root".getBytes());

        fileStorage.saveObject(USER_ID, "", rootFile);
        fileStorage.saveObject(USER_ID, "root/folder/", firstFile);
        fileStorage.saveObject(USER_ID, "root/folder/", secondFile);
        fileStorage.createPath(USER_ID, "File/");
        fileStorage.createPath(USER_ID, "NotContain/FolderName/");

        List<StorageObjectInfo> infos = fileStorage.searchObjectInfo(USER_ID, "", "File");

        assertThat(infos).hasSize(3);
    }
}
