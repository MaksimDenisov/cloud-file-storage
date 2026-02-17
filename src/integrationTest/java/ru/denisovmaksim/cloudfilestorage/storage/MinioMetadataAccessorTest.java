package ru.denisovmaksim.cloudfilestorage.storage;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.denisovmaksim.cloudfilestorage.service.fixture.StorageFixture;
import ru.denisovmaksim.cloudfilestorage.storage.assertion.StorageObjectInfoAssert;
import ru.denisovmaksim.cloudfilestorage.storage.assertion.StorageObjectInfoListAssert;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.denisovmaksim.cloudfilestorage.storage.fixtures.MinioFixture.MINIO_EXCEED_PREFIX_LENGTH;
import static ru.denisovmaksim.cloudfilestorage.storage.fixtures.MinioFixture.USER_ID;

@SpringJUnitConfig
@Testcontainers
@Import(StorageMetadataAccessor.class)
class MinioMetadataAccessorTest extends AbstractMinioIntegrationTest {

    @Autowired
    private StorageMetadataAccessor metadataAccessor;

    @BeforeEach
    void cleanUp() throws Exception {
        fixture.clearAll();
    }

    @Test
    @DisplayName("Create new path.")
    void shouldCreatePath() throws Exception {
        String path = "/folder/subfolder/";
        metadataAccessor.createPath(StorageFixture.USER_ID, path);
        Optional<StorageObjectInfo> info = metadataAccessor.getOne(USER_ID,path);
        assertThat(info.isPresent()).isTrue();
        StorageObjectInfoAssert.assertThat(info.get())
                .isDirectory()
                .hasName("subfolder")
                .hasPath( "folder/subfolder/")
                .hasSize(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"..", "*", "|", "\\", "\"", ";"})
    @DisplayName("If path contains not accepted chars should throw exception.")
    void shouldThrowExceptionWhenCreateNotValidPath(String notValidPath) {
        assertThrows(IllegalArgumentException.class, () ->
                metadataAccessor.createPath(StorageFixture.USER_ID, notValidPath)
        );
    }

    @Test
    @DisplayName("If path length exceed 1024 bytes should throw exception.")
    void shouldThrowExceptionWhenCreateVeryLongPath() {
        String veryLongPath = "a".repeat(MINIO_EXCEED_PREFIX_LENGTH);
        assertThrows(IllegalArgumentException.class, () ->
                metadataAccessor.createPath(StorageFixture.USER_ID, veryLongPath)
        );
    }

    @Test
    @DisplayName("Accessing metadata of a non-existent file should return empty")
    void shouldReturnFalseOrEmptyWhenAccessingNonExistentFileMetadata()  throws Exception {
        assertThat(metadataAccessor.getOne(USER_ID, "not-exist-path"))
                .isNotPresent();
    }

    @Test
    @DisplayName("Accessing metadata of a non-existent path should return false/empty")
    void shouldReturnFalseOrEmptyWhenAccessingNonExistentPathMetadata() throws Exception {
        assertThat(metadataAccessor.isExistByPrefix(USER_ID, "not-exist-path"))
                .isFalse();
        assertThat(metadataAccessor.listObjectInfo(USER_ID, "not-exist-path"))
                .isNotPresent();
    }

    @Test
    @DisplayName("Root folder metadata should always be present")
    void shouldAlwaysReturnMetadataForRootFolder() throws Exception {
        assertThat(metadataAccessor.listObjectInfo(USER_ID, "/"))
                .isPresent();
        assertThat(metadataAccessor.listObjectInfo(USER_ID, ""))
                .isPresent();
    }

    @Test
    @DisplayName("search() should return DTOs based on query")
    void shouldReturnObjectInfosWhenSearchingBySubstring() throws Exception {
        fixture.folder("user-1-files/folder/");
        fixture.folder("user-1-files/folder/folder");
        fixture.folder("user-1-files/folder/folder/folder.txt");
        fixture.file("user-1-files/folder/file.txt", "content");
        fixture.file("user-1-files/file.txt", "content");

        List<StorageObjectInfo> paths =
                metadataAccessor.findObjectInfosBySubstring(USER_ID, "/", "file");

        StorageObjectInfoListAssert.assertThat(paths)
                .containsExactlyPaths("folder/file.txt", "file.txt");
    }
}
