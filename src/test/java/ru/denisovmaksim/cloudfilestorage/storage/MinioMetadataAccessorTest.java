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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@Deprecated
class MinioMetadataAccessorTest {

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
    private MinioMetadataAccessor minioMetadataAccessor;





    @Test
    void getStorageObjectsByNotExistPath() {
        assertFalse(minioMetadataAccessor.isExist(USER_ID, "not-exist-path"));
        assertFalse(minioMetadataAccessor.listObjectInfo(USER_ID, "not-exist-path").isPresent());
    }

    @Test
    void getStorageObjectsByRoot() {
        assertTrue(minioMetadataAccessor.listObjectInfo(USER_ID, "").isPresent());
    }


}
