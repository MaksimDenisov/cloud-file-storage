package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.denisovmaksim.cloudfilestorage.exception.FileStorageException;
import ru.denisovmaksim.cloudfilestorage.storage.assertion.StorageObjectInfoListAssert;
import ru.denisovmaksim.cloudfilestorage.storage.fixtures.MinioFixture;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.denisovmaksim.cloudfilestorage.storage.fixtures.MinioFixture.BUCKET;
import static ru.denisovmaksim.cloudfilestorage.storage.fixtures.MinioFixture.USER_ID;


@SpringJUnitConfig
@Testcontainers
@Import({MinioMetadataAccessor.class,
        MinioPathResolver.class,
        MinioObjectFetcher.class,
        MinioFixture.class})
class MinioMetadataAccessorTest {
    @Container
    private static final MinIOContainer MINIO_CONTAINER = new MinIOContainer("minio/minio");

    @Autowired
    private MinioMetadataAccessor minioMetadataAccessor;

    @Autowired
    private MinioFixture fixture;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("minio.url", () -> "http://" + MINIO_CONTAINER.getHost() +
                ":" + MINIO_CONTAINER.getMappedPort(9000));
        registry.add("minio.access-key", MINIO_CONTAINER::getUserName);
        registry.add("minio.secret-key", MINIO_CONTAINER::getPassword);
        registry.add("minio.bucket", () -> BUCKET);
        registry.add("app.bucket", () -> BUCKET);
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        public MinioClient minioClient(
                @Value("${minio.url}") String endpoint,
                @Value("${minio.access-key}") String accessKey,
                @Value("${minio.secret-key}") String secretKey,
                @Value("${minio.bucket}") String bucket) {
            try {
                MinioClient minioClient =
                        MinioClient.builder()
                                .endpoint(endpoint)
                                .credentials(accessKey, secretKey)
                                .build();
                boolean found =
                        minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
                if (!found) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                }
                return minioClient;
            } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new FileStorageException(e);
            }
        }
    }

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
