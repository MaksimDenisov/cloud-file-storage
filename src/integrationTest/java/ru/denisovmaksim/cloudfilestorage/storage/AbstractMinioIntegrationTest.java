package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import ru.denisovmaksim.cloudfilestorage.config.MinioProperties;
import ru.denisovmaksim.cloudfilestorage.storage.fixtures.MinioFixture;

@Import({MinioTestConfiguration.class,
        MinioDataAccessor.class,
        MinioMetadataAccessor.class,
        MinioPathResolver.class,
        MinioObjectFetcher.class,
        MinioFixture.class,
        StorageObjectInfoMapper.class})
@EnableConfigurationProperties(MinioProperties.class)
public abstract class AbstractMinioIntegrationTest {

    public static final String BUCKET = "user-files";

    @Autowired
    protected MinioClient minioClient;

    protected static final MinIOContainer MINIO_CONTAINER =
            MinioTestContainer.getInstance();

    @Autowired
    protected MinioDataAccessor minioDataAccessor;

    @Autowired
    protected MinioMetadataAccessor minioMetadataAccessor;

    @Autowired
    protected MinioFixture fixture;
    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", () -> "http://" + MINIO_CONTAINER.getHost() +
                ":" + MINIO_CONTAINER.getMappedPort(9000));
        registry.add("minio.access-key", MINIO_CONTAINER::getUserName);
        registry.add("minio.secret-key", MINIO_CONTAINER::getPassword);
        registry.add("minio.bucket", () -> BUCKET);
        registry.add("app.bucket", () -> BUCKET);
    }
}
