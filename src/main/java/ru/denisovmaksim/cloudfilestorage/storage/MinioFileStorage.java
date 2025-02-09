package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.exception.StorageObjectNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Component
@Slf4j
@Profile({"dev", "prod"})
public class MinioFileStorage {
    // https://min.io/docs/minio/linux/developers/java/minio-java.html
    // Examples
    // https://github.com/minio/minio-java/tree/release/examples

    //https://min.io/docs/minio/linux/developers/java/API.html
    private final MinioClient minioClient;

    private final String bucket;

    public MinioFileStorage(MinioClient minioClient, @Value("${app.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    public void createEmptyPath(Long userId, String path, String folderName) {
        log.info("Create folder path = {} name = {} for user with id = {}", path, folderName, userId);
        MinioPath minioPath = new MinioPath(userId, path);
        MinioExceptionHandler.interceptMinioExceptions(() -> {
                    String newFolderName = minioPath.getPathByMinio() + folderName + "/";
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(newFolderName)
                                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                                    .build());
                }
        );
    }

    public List<StorageObject> getStorageObjects(Long userId, String path) {
        log.info("Get objects from path = {} for user with id = {}", path, userId);
        MinioPath minioPath = new MinioPath(userId, path);
        Iterable<Result<Item>> resultItems = getMinioItems(minioPath, false);
        List<StorageObject> objects = new ArrayList<>();
        for (Result<Item> resultItem : resultItems) {
            Item item = MinioExceptionHandler.interceptMinioExceptions(resultItem::get);
            String minioName = item.objectName();
            if (minioPath.isNotSame(minioName)) {
                String objectPath = minioPath.extractPathByUser(minioName);
                StorageObject object = new StorageObject(objectPath);
                // For a folder, the size is the number of objects it contains
                long size = object.isFolder() ? getChildCount(new MinioPath(userId, objectPath)) : item.size();
                object.setSize(size);
                objects.add(object);
            }
        }
        return objects;
    }

    public void renameFolder(Long userId, String path, String newFolderName) {
        log.info("Rename folder {} for user with id = {}", path, userId);
        MinioPath minioPath = new MinioPath(userId, path);
        MinioExceptionHandler.interceptMinioExceptions(() -> {
            Iterable<Result<Item>> minioItems = getMinioItems(new MinioPath(userId, path), true);
            for (Result<Item> result : minioItems) {
                String sourceName = result.get().objectName();
                String destName = sourceName.replace(minioPath.getPathByMinio(),
                        minioPath.getParentMinioPath() + newFolderName + "/");
                copyObject(sourceName, destName);
            }
        });
        deleteObjects(userId, path);
    }

    public void deleteObjects(Long userId, String path) {
        log.info("Delete folder {} for user with id = {}", path, userId);
        MinioPath minioPath = new MinioPath(userId, path);
        // User folder cannot be deleted
        if (!minioPath.isRoot()) {
            MinioExceptionHandler.interceptMinioExceptions(() -> {
                Iterable<Result<Item>> minioItems = getMinioItems(minioPath, true);
                for (Result<Item> resultItem : minioItems) {
                    minioClient.removeObject(
                            RemoveObjectArgs.builder().bucket(bucket)
                                    .object(resultItem.get().objectName())
                                    .build());
                }
            });
        }
    }

    public void saveObject(Long userId, String path, MultipartFile file) {
        MinioPath minioPath = new MinioPath(userId, path);
        MinioExceptionHandler.interceptMinioExceptions(() -> {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(minioPath.getPathByMinio() + file.getOriginalFilename())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        });
    }

    public StorageObjectStream getObjectAsStream(Long userId, String path) {
        MinioPath minioPath = new MinioPath(userId, path);
        return MinioExceptionHandler.interceptMinioExceptions(() -> {
            GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(minioPath.getPathByMinio())
                    .build());
            return new StorageObjectStream(path, response);
        });
    }

    public StorageObjectsStreams getObjectsAsStreams(Long userId, String path) {
        MinioPath minioPath = new MinioPath(userId, path);
        Map<String, InputStream> result = new HashMap<>();
        return MinioExceptionHandler.interceptMinioExceptions(() -> {
            Iterable<Result<Item>> minioItems = getMinioItems(minioPath, true);
            for (Result<Item> resultItem : minioItems) {
                String objectName = resultItem.get().objectName();
                String name = objectName.replace(minioPath.getPathByMinio(), "");
                if (name.isEmpty()) {
                    continue;
                }
                InputStream objectInputStream = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build());
                result.put(name, objectInputStream);
            }
            return new StorageObjectsStreams(path, result);
        });
    }

    private Iterable<Result<Item>> getMinioItems(MinioPath minioPath, boolean includeSubObjects) {
        Iterable<Result<Item>> minioItems = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .recursive(includeSubObjects)
                        .prefix(minioPath.getPathByMinio())
                        .build());
        // If storage is empty. Root not contain objects. It is not an error
        if (!minioItems.iterator().hasNext() && !minioPath.isRoot()) {
            throw new StorageObjectNotFoundException(String.format("%s not exist", minioPath.getPathByUser()));
        }
        return minioItems;
    }

    private long getChildCount(MinioPath minioPath) {
        Iterable<Result<Item>> minioItems = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(minioPath.getPathByMinio())
                        .build());
        return StreamSupport.stream(minioItems.spliterator(), false)
                .map(item -> MinioExceptionHandler.interceptMinioExceptions(item::get))
                .filter(item -> !item.objectName().equals(minioPath.getPathByMinio()))
                .count();
    }

    private void copyObject(String sourceName, String destName) {
        MinioExceptionHandler.interceptMinioExceptions(() ->
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucket)
                                .object(destName)
                                .source(
                                        CopySource.builder()
                                                .bucket(bucket)
                                                .object(sourceName)
                                                .build())
                                .build()));
    }
}
