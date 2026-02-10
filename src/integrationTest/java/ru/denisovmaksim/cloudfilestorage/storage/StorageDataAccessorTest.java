package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.ListObjectsArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.denisovmaksim.cloudfilestorage.storage.assertion.StorageObjectAssert;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.denisovmaksim.cloudfilestorage.service.fixture.StorageFixture.USER_ID;

@SpringJUnitConfig
@Testcontainers
@Import(StorageDataAccessor.class)
class StorageDataAccessorTest extends AbstractMinioIntegrationTest {

    @Autowired
    StorageDataAccessor storageDataAccessor;

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

    @Test
    @DisplayName("Deleting root folder should not remove it from storage")
    void shouldNotDeleteRootFolder() {
        storageDataAccessor.deleteObjects(USER_ID, "");
        assertTrue(minioMetadataAccessor.listObjectInfo(USER_ID, "").isPresent());
        storageDataAccessor.deleteOneObject(USER_ID, "");
        assertTrue(minioMetadataAccessor.listObjectInfo(USER_ID, "").isPresent());
    }

    @Test
    @DisplayName("Given valid file when saved then it can be retrieved and deleted")
    void saveGetAndDeleteObject() throws Exception {
        byte[] expectedBytes = "Hello".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile("file.txt", "file.txt",
                "text/plain", expectedBytes);

        storageDataAccessor.saveObject(USER_ID, "folder/", file);

        StorageObject actual = storageDataAccessor.getObject(USER_ID, "folder/file.txt");
        StorageObjectAssert.assertThat(actual)
                .hasPath("folder/file.txt")
                .matchContent(expectedBytes);

        storageDataAccessor.deleteOneObject(USER_ID, "folder/file.txt");

        Optional<StorageObjectInfo> deleted = minioMetadataAccessor.getOne(USER_ID, "folder/file.txt");
        Assertions.assertThat(deleted).isNotPresent();

        Assertions.assertThatThrownBy(() ->
                storageDataAccessor.getObject(USER_ID, "folder/file.txt")
        ).isInstanceOf(FileStorageException.class);
    }

    @Test
    @DisplayName("Given multiple files when saved then they can be retrieved")
    void getManyObjects() throws Exception {
        byte[] expectedBytes1 = "First".getBytes(StandardCharsets.UTF_8);
        byte[] expectedBytes2 = "Second".getBytes(StandardCharsets.UTF_8);

        MultipartFile firstFile = new MockMultipartFile("firstFile.txt", "firstFile.txt",
                "text/plain", expectedBytes1);
        MultipartFile secondFile = new MockMultipartFile("secondFile.txt", "secondFile.txt",
                "text/plain", expectedBytes2);
        storageDataAccessor.saveObject(USER_ID, "folder/", firstFile);
        storageDataAccessor.saveObject(USER_ID, "folder/", secondFile);

        List<StorageObject> actual = storageDataAccessor.getObjects(USER_ID, "folder/");

        Assertions.assertThat(actual)
                .hasSize(2)
                .extracting(StorageObject::path)
                .containsExactlyInAnyOrder(
                        "folder/firstFile.txt",
                        "folder/secondFile.txt"
                );
        Map<String, byte[]> expected = Map.of(
                "folder/firstFile.txt", expectedBytes1,
                "folder/secondFile.txt", expectedBytes2
        );
        for (StorageObject object : actual) {
            byte[] expectedContent = expected.get(object.path());
            Assertions.assertThat(expectedContent)
                    .as("Unexpected object path: " + object.path())
                    .isNotNull();
            StorageObjectAssert.assertThat(object)
                    .matchContent(expectedContent);
        }
    }

    @Test
    @DisplayName("Given existing object when copied then new object is created at target path")
    void copyObject() throws Exception {
        byte[] expectedBytes = "Hello".getBytes(StandardCharsets.UTF_8);
        MultipartFile file = new MockMultipartFile("file.txt", "file.txt",
                "text/plain", expectedBytes);
        storageDataAccessor.saveObject(USER_ID, "folder/", file);


        storageDataAccessor.copyOneObject(USER_ID, "folder/file.txt", "folder/copy-file.txt");

        StorageObject actual = storageDataAccessor.getObject(USER_ID, "folder/copy-file.txt");

        StorageObjectAssert.assertThat(actual)
                .hasPath("folder/copy-file.txt")
                .matchContent(expectedBytes);
    }

    @Test
    @DisplayName("Given multiple objects when copied then all objects are copied successfully")
    void copyManyObjects() throws Exception {
        byte[] expectedBytes1 = "First".getBytes(StandardCharsets.UTF_8);
        byte[] expectedBytes2 = "Second".getBytes(StandardCharsets.UTF_8);

        MultipartFile firstFile = new MockMultipartFile("firstFile.txt", "firstFile.txt",
                "text/plain", expectedBytes1);
        MultipartFile secondFile = new MockMultipartFile("secondFile.txt", "secondFile.txt",
                "text/plain", expectedBytes2);
        storageDataAccessor.saveObject(USER_ID, "folder/", firstFile);
        storageDataAccessor.saveObject(USER_ID, "folder/", secondFile);

        storageDataAccessor.copyObjects(USER_ID, "folder/", "copiedFolder/");

        List<StorageObject> actual = storageDataAccessor.getObjects(USER_ID, "copiedFolder/");

        Assertions.assertThat(actual)
                .hasSize(2)
                .extracting(StorageObject::path)
                .containsExactlyInAnyOrder(
                        "copiedFolder/firstFile.txt",
                        "copiedFolder/secondFile.txt"
                );
        Map<String, byte[]> expected = Map.of(
                "copiedFolder/firstFile.txt", expectedBytes1,
                "copiedFolder/secondFile.txt", expectedBytes2
        );
        for (StorageObject object : actual) {
            byte[] expectedContent = expected.get(object.path());
            Assertions.assertThat(expectedContent)
                    .as("Unexpected object path: " + object.path())
                    .isNotNull();
            StorageObjectAssert.assertThat(object)
                    .matchContent(expectedContent);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "0,4",
            "0,26",
            "13,13"
    })
    @DisplayName("Should return correct byte range from object")
    void shouldReturnCorrectRangeFromObject(long rangeStart, long contentLength) throws Exception {
        byte[] content = "Hello, this is a test file".getBytes(StandardCharsets.UTF_8);
        String path = "folder/file.txt";
        MultipartFile file = new MockMultipartFile("file", "file.txt",
                "text/plain", content);

        storageDataAccessor.saveObject(USER_ID, "folder/", file);

        try (InputStream rangeStream = storageDataAccessor.getRangeOfObject(USER_ID, path, rangeStart, contentLength)) {
            byte[] rangeBytes = rangeStream.readAllBytes();
            byte[] expectedRange = Arrays.copyOfRange(content, (int) rangeStart, (int) (rangeStart + contentLength));
            Assertions.assertThat(rangeBytes).isEqualTo(expectedRange);
        }
    }

}
