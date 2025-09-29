package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.denisovmaksim.cloudfilestorage.IntegrationTestConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(IntegrationTestConfiguration.class)
@Deprecated
class MinioDataAccessorTest {
    private static final Long USER_ID = 1L;
    private static final String BUCKET = "user-files";

    @BeforeEach
    void cleanUp() throws Exception {
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
    private MinioDataAccessor minioDataAccessor;


    @Autowired
    private MinioMetadataAccessor minioMetadataAccessor;

    @Test
    void createPath() {
        minioDataAccessor.createPath(USER_ID, "folder/");

        StorageObjectInfo expectedObject = new StorageObjectInfo("folder/", "folder", true, 0);

        List<StorageObjectInfo> actualObjects = minioMetadataAccessor.listObjectInfo(USER_ID, "").get();

        Assertions.assertThat(actualObjects)
                .as("Count of objects")
                .hasSize(1)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(expectedObject);
    }

    @ParameterizedTest
    @ValueSource(strings = {"..", "*", "|", "\\", "\"", ";"})
    void createNotValidPath(String notValidPath) {
        assertThrows(IllegalArgumentException.class, () ->
                minioDataAccessor.createPath(USER_ID, notValidPath)
        );
    }

    @Test
    void createVeryLongPath() {
        String veryLongPath = "a".repeat(1025);
        assertThrows(IllegalArgumentException.class, () ->
                minioDataAccessor.createPath(USER_ID, veryLongPath)
        );
    }

    @Test
    void saveAndGetObject() throws IOException {
        MultipartFile file = new MockMultipartFile("file.txt", "file.txt",
                "text/plain", "Hello".getBytes());

        minioDataAccessor.saveObject(USER_ID, "folder/", file);

        StorageObject actual = minioDataAccessor.getObject(USER_ID, "folder/file.txt");

        try (InputStream actualStream = actual.stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            Assertions.assertThat(actualBytes)
                    .as("Match file content")
                    .isEqualTo(file.getBytes());
        }

        assertTrue(minioMetadataAccessor.isExist(USER_ID, "folder/"));
        assertTrue(minioMetadataAccessor.isExist(USER_ID, "folder/file.txt"));
        Assertions.assertThat(actual.path())
                .as("Match path")
                .isEqualTo("folder/file.txt");
    }

    @Test
    void saveAndDeleteObjects() {
        MultipartFile file = new MockMultipartFile("file.txt", "file.txt", "text/plain", "Hello".getBytes());
        minioDataAccessor.saveObject(USER_ID, "folder/subFolder1", file);
        minioDataAccessor.saveObject(USER_ID, "folder/subFolder2", file);
        Assertions.assertThat(minioMetadataAccessor.listObjectInfo(USER_ID, "folder/").get())
                .hasSize(2);

        minioDataAccessor.deleteObjects(USER_ID, "folder/");
        Optional<List<StorageObjectInfo>> infos = minioMetadataAccessor.listObjectInfo(USER_ID, "");
        Assertions.assertThat(infos).isPresent();
        Assertions.assertThat(infos.get()).isEmpty();
    }

    @Test
    void getManyObjects() throws IOException {
        MultipartFile firstFile = new MockMultipartFile("firstFile.txt", "firstFile.txt",
                "text/plain", "First".getBytes());
        MultipartFile secondFile = new MockMultipartFile("secondFile.txt", "secondFile.txt",
                "text/plain", "Second".getBytes());
        minioDataAccessor.saveObject(USER_ID, "folder/", firstFile);
        minioDataAccessor.saveObject(USER_ID, "folder/", secondFile);

        List<StorageObject> actual = minioDataAccessor.getObjects(USER_ID, "folder/");

        Assertions.assertThat(actual).hasSize(2);

        try (InputStream actualStream = actual.get(0).stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            Assertions.assertThat(actualBytes)
                    .as("Match first file content")
                    .isEqualTo(firstFile.getBytes());
        }

        try (InputStream actualStream = actual.get(1).stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            Assertions.assertThat(actualBytes)
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
        minioDataAccessor.saveObject(USER_ID, "root/folder/", firstFile);
        minioDataAccessor.saveObject(USER_ID, "root/folder/", secondFile);

        minioDataAccessor.copyObjects(USER_ID, "root/folder/", "root/copiedFolder/");

        List<StorageObject> actual = minioDataAccessor.getObjects(USER_ID, "root/copiedFolder/");

        Assertions.assertThat(actual).hasSize(2);

        try (InputStream actualStream = actual.get(0).stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            Assertions.assertThat(actualBytes)
                    .as("Match first file content")
                    .isEqualTo(firstFile.getBytes());
        }

        try (InputStream actualStream = actual.get(1).stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            Assertions.assertThat(actualBytes)
                    .as("Match second file content")
                    .isEqualTo(secondFile.getBytes());
        }
    }

    @Test
    void shouldNotDeleteRootFolder() {
        minioDataAccessor.deleteObjects(USER_ID, "");
        assertTrue(minioMetadataAccessor.listObjectInfo(USER_ID, "").isPresent());
    }

    @Test
    void copyObject() throws IOException {
        MultipartFile file = new MockMultipartFile("file.txt", "file.txt", "text/plain", "Hello".getBytes());
        minioDataAccessor.saveObject(USER_ID, "folder/", file);

        minioDataAccessor.copyOneObject(USER_ID, "folder/file.txt", "folder/copy-file.txt");

        StorageObject actual = minioDataAccessor.getObject(USER_ID, "folder/copy-file.txt");

        try (InputStream actualStream = actual.stream()) {
            byte[] actualBytes = actualStream.readAllBytes();
            Assertions.assertThat(actualBytes)
                    .as("Match file content")
                    .isEqualTo(file.getBytes());
        }

        Assertions.assertThat(actual.path())
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

        minioDataAccessor.saveObject(USER_ID, "", rootFile);
        minioDataAccessor.saveObject(USER_ID, "folder/", firstFile);
        minioDataAccessor.saveObject(USER_ID, "folder/", secondFile);
        minioDataAccessor.createPath(USER_ID, "emptyFolder/");

        Long rootCount = minioMetadataAccessor.getDirectChildCount(USER_ID, "");
        Long folderCount = minioMetadataAccessor.getDirectChildCount(USER_ID, "folder/");
        Long emptyCount = minioMetadataAccessor.getDirectChildCount(USER_ID, "emptyFolder/");

        Assertions.assertThat(rootCount).isEqualTo(3L);
        Assertions.assertThat(folderCount).isEqualTo(2L);
        Assertions.assertThat(emptyCount).isZero();
    }

    @Test
    void shouldSearchObjects() {
        MultipartFile firstFile = new MockMultipartFile("firstFile.txt", "firstFile.txt",
                "text/plain", "First".getBytes());
        MultipartFile secondFile = new MockMultipartFile("secondFile.txt", "secondFile.txt",
                "text/plain", "Second".getBytes());
        MultipartFile rootFile = new MockMultipartFile("root.txt", "root.txt",
                "text/plain", "Root".getBytes());

        minioDataAccessor.saveObject(USER_ID, "", rootFile);
        minioDataAccessor.saveObject(USER_ID, "root/folder/", firstFile);
        minioDataAccessor.saveObject(USER_ID, "root/folder/", secondFile);
        minioDataAccessor.createPath(USER_ID, "File/");
        minioDataAccessor.createPath(USER_ID, "NotContain/FolderName/");

        List<StorageObjectInfo> infos = minioMetadataAccessor.searchObjectInfo(USER_ID, "", "File");

        Assertions.assertThat(infos).hasSize(3);
    }
}
