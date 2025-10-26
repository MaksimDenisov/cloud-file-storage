package ru.denisovmaksim.cloudfilestorage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MySQLContainer;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@TestConfiguration
public class IntegrationTestConfiguration {
    private static final String BUCKET = "user-files";
    private static final MinIOContainer MINIO_CONTAINER;
    private static final MySQLContainer<?> MY_SQL_CONTAINER;

    static {
        MINIO_CONTAINER = new MinIOContainer("minio/minio")
                .withExposedPorts(9000)
                .withEnv("MINIO_ROOT_USER", "user")
                .withEnv("MINIO_ROOT_PASSWORD", "password")
                .withCommand("server /data");

        MY_SQL_CONTAINER = new MySQLContainer<>("mysql:8.0.26")
                .withDatabaseName("db_name")
                .withUsername("root")
                .withPassword("password");

        if (!MY_SQL_CONTAINER.isRunning()) {
            MY_SQL_CONTAINER.start();
        }
        if (!MINIO_CONTAINER.isRunning()) {
            MINIO_CONTAINER.start();
        }
    }

    @Bean(destroyMethod = "stop")
    public MinIOContainer minioContainer() {
        return MINIO_CONTAINER;
    }

    @Bean(destroyMethod = "stop")
    public MySQLContainer<?> mySQLContainer() {
        return MY_SQL_CONTAINER;
    }


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

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", () ->
                "http://" + MINIO_CONTAINER.getHost() + ":" + MINIO_CONTAINER.getMappedPort(9000));
        registry.add("minio.access-key", () -> "user");
        registry.add("minio.secret-key", () -> "password");
        registry.add("minio.bucket", () -> BUCKET);

        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
    }

}
