package ru.denisovmaksim.cloudfilestorage.storage.fixtures;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@TestComponent
public class MinioFixture {
    public static final Long USER_ID = 1L;
    public static final String BUCKET = "user-files";
    private MinioClient minioClient;

    public MinioFixture(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void clearAll() throws Exception {
        Iterable<Result<Item>> objects = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(BUCKET)
                .recursive(true)
                .build());

        for (Result<Item> object : objects) {
            String objectName = object.get().objectName();
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(BUCKET).object(objectName).build());
        }
    }

    public void folder(String path) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(path)
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .build());
    }

    public void file(String path, String content) throws Exception {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(path)
                        .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                        .contentType("text/plain; charset=utf-8")
                        .build());
    }
}
