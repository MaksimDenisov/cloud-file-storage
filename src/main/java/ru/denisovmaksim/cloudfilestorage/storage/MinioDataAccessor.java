package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.config.MinioProperties;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
class MinioDataAccessor {
    private final MinioClient minioClient;
    private final MinioProperties properties;
    private final MinioPathResolver resolver;
    private final MinioObjectFetcher objectFetcher;

    public StorageObject getObject(Long userId, String path) throws MinioException, IOException,
            NoSuchAlgorithmException, InvalidKeyException {
        String minioPath = resolver.resolveMinioPath(userId, path);
        InputStream objectInputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.bucket())
                .object(minioPath)
                .build());
        return new StorageObject(path, objectInputStream);
    }

    public InputStream getRangeOfObject(Long userId, String path, Long rangeStart, Long contentLength)
            throws MinioException, IOException,
            NoSuchAlgorithmException, InvalidKeyException {
        String minioPath = resolver.resolveMinioPath(userId, path);
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.bucket())
                        .object(minioPath)
                        .offset(rangeStart)
                        .length(contentLength)
                        .build());
    }


    public List<StorageObject> getObjects(Long userId, String path) throws MinioException, IOException,
            NoSuchAlgorithmException, InvalidKeyException {
        List<Item> minioItems = objectFetcher.getMinioItems(userId, path, true).orElseThrow();
        List<StorageObject> fileObjects = new ArrayList<>();
        for (Item item : minioItems) {
            String objectName = item.objectName();
            String objectPath = resolver.resolvePathFromMinioObjectName(userId, objectName);
            InputStream objectInputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectName)
                    .build());
            fileObjects.add(new StorageObject(objectPath, objectInputStream));
        }
        return fileObjects;
    }


    public void saveObject(Long userId, String path, MultipartFile file) throws MinioException, IOException,
            NoSuchAlgorithmException, InvalidKeyException {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(properties.bucket())
                        .object(resolver.resolveMinioPath(userId, PathUtil.ensureDirectoryPath(path)
                                + file.getOriginalFilename()))
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
    }


    public void copyOneObject(Long userId, String srcPath, String destPath) throws MinioException, IOException,
            NoSuchAlgorithmException, InvalidKeyException {
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(properties.bucket())
                        .object(resolver.resolveMinioPath(userId, destPath))
                        .source(
                                CopySource.builder()
                                        .bucket(properties.bucket())
                                        .object(resolver.resolveMinioPath(userId, srcPath))
                                        .build())
                        .build());
    }


    public int copyObjects(Long userId, String srcPath, String destPath) throws MinioException, IOException,
            NoSuchAlgorithmException, InvalidKeyException {
        String srcMinioPath = resolver.resolveMinioPath(userId, srcPath);
        AtomicInteger count = new AtomicInteger();

        List<Item> minioItems = objectFetcher.getMinioItems(userId, srcPath, true)
                .orElseThrow();
        for (Item item : minioItems) {
            String fromPath = resolver.resolvePathFromMinioObjectName(userId, item.objectName());
            String toPath = item.objectName()
                    .replaceFirst(srcMinioPath, destPath);
            count.incrementAndGet();
            copyOneObject(userId, fromPath, toPath);
        }
        return count.get();
    }

    public void deleteOneObject(Long userId, String path) throws MinioException, IOException,
            NoSuchAlgorithmException, InvalidKeyException {
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(properties.bucket())
                        .object(resolver.resolveMinioPath(userId, path))
                        .build());
    }

    public void deleteObjects(Long userId, String path) throws MinioException, IOException,
            NoSuchAlgorithmException, InvalidKeyException {
        Optional<List<Item>> items = objectFetcher.getMinioItems(userId, path, true);
        if (items.isPresent()) {
            for (Item item : items.get()) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder().bucket(properties.bucket())
                                .object(item.objectName())
                                .build());
            }
        }
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(properties.bucket())
                        .object(resolver.resolveMinioPath(userId, path))
                        .build());
    }
}
