package ru.denisovmaksim.cloudfilestorage.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.denisovmaksim.cloudfilestorage.storage.assertion.StorageObjectInfoListAssert;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.denisovmaksim.cloudfilestorage.storage.fixtures.MinioFixture.USER_ID;


@SpringJUnitConfig
@Testcontainers
class MinioMetadataAccessorTest extends AbstractMinioIntegrationTest {

    @BeforeEach
    void cleanUp() throws Exception {
        fixture.clearAll();
    }

    @Test
    @DisplayName("Accessing metadata of a non-existent path should return false/empty")
    void shouldReturnFalseOrEmptyWhenAccessingNonExistentPathMetadata() {
        assertThat(minioMetadataAccessor.isExist(USER_ID, "not-exist-path"))
                .isFalse();
        assertThat(minioMetadataAccessor.listObjectInfo(USER_ID, "not-exist-path"))
                .isNotPresent();
    }

    @Test
    @DisplayName("Root folder metadata should always be present")
    void shouldAlwaysReturnMetadataForRootFolder() {
        assertThat(minioMetadataAccessor.listObjectInfo(USER_ID, "/"))
                .isPresent();
        assertThat(minioMetadataAccessor.listObjectInfo(USER_ID, ""))
                .isPresent();
    }

    @Test
    @DisplayName("Getting size of a non-existent path should return -1")
    void shouldReturnMinusOneWhenGettingSizeOfNonExistentPath() {
        assertThat(minioMetadataAccessor.getSize(USER_ID, "not-exist-path"))
                .isEqualTo(-1);
    }

    @Test
    @DisplayName("Getting size of an existing file should return its size")
    void shouldReturnFileSizeWhenGettingSizeOfExistingFile() throws Exception {
        String content = "Content";
        fixture.file("user-1-files/file.txt", content);

        assertThat(minioMetadataAccessor.getSize(USER_ID, "file.txt"))
                .isEqualTo(content.length());
    }

    @Test
    @DisplayName("Getting direct child count of a non-existent path should return 0")
    void shouldReturnZeroWhenGettingDirectChildCountOfNonExistentPath() {
        assertThat(minioMetadataAccessor.getDirectChildCount(USER_ID, "not-exist-path"))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("Getting direct child count of a existent path should return its sum of files and folders")
    void shouldReturnSumOfFilesAndFoldersWhenGettingDirectChildCountOfExistingPath() throws Exception  {
        fixture.folder("user-1-files/folder/");
        fixture.folder("user-1-files/folder/folder");
        fixture.file("user-1-files/folder/file.txt","content");
        fixture.file("user-1-files/file.txt", "content");

        assertThat(minioMetadataAccessor.getDirectChildCount(USER_ID, "/"))
                .isEqualTo(2);
        assertThat(minioMetadataAccessor.getDirectChildCount(USER_ID, "folder/"))
                .isEqualTo(2);
        assertThat(minioMetadataAccessor.getDirectChildCount(USER_ID, "folder/folder/"))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("search() should return DTOs based on query")
    void shouldReturnObjectInfosWhenSearchingBySubstring() throws Exception  {
        fixture.folder("user-1-files/folder/");
        fixture.folder("user-1-files/folder/folder");
        fixture.folder("user-1-files/folder/folder/folder.txt");
        fixture.file("user-1-files/folder/file.txt","content");
        fixture.file("user-1-files/file.txt", "content");

        List<StorageObjectInfo> paths =
                minioMetadataAccessor.findObjectInfosBySubstring(USER_ID, "/", "file");

        StorageObjectInfoListAssert.assertThat(paths)
                .containsExactlyPaths("folder/file.txt","file.txt");
    }
}
