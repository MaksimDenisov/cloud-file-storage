package ru.denisovmaksim.cloudfilestorage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.denisovmaksim.cloudfilestorage.exception.FileStorageException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


@Configuration()
@Profile({"dev", "prod"})
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.user}")
    private String user;
    @Value("${minio.password}")
    private String password;
    @Value("${app.bucket}")
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(endpoint)
                            .credentials(user, password)
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
