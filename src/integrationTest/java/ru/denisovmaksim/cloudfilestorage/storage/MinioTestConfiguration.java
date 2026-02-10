package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@TestConfiguration
public class MinioTestConfiguration {
    @Bean
    public MinioClient minioClient(
            @Value("${minio.url}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket}") String bucket) {

        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());

            if (!exists) {
                client.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build());
            }

            return client;
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new FileStorageException(e);
        }
    }
}
