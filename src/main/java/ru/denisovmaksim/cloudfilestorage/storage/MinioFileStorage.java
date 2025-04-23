package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Slf4j
public class MinioFileStorage {
    private final MinioClient minioClient;
    private final String bucket;
    private final MinioPathResolver resolver;

    public MinioFileStorage(MinioClient minioClient,
                            MinioPathResolver resolver,
                            @Value("${app.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.resolver = resolver;
        this.bucket = bucket;
    }

    public void createPath(Long userId, String path) {
        log.info("Create path = {} for user with id = {}", path, userId);
        MinioPath minioPath = resolver.resolve(userId, path);
        MinioExceptionHandler.interceptMinioExceptions(() -> {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(minioPath.getPathByMinio())
                                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                                    .build());
                }
        );
    }

    public Optional<List<StorageObjectInfo>> getPathContent(Long userId, String path) {
        log.info("Get objects from path = {} for user with id = {}", path, userId);
        MinioPath minioPath = resolver.resolve(userId, path);
        return getMinioItems(minioPath, false)
                .map(list -> list.stream()
                        .map(item -> {
                            String objectPath = resolver.resolvePathFromMinioObjectName(userId, item.objectName());
                            return new StorageObjectInfo.Builder(objectPath)
                                    .objectSize(item.size())
                                    .withFolderSizeSupplier(() ->
                                            getChildCount(resolver.resolve(userId, objectPath)))
                                    .build();
                        })
                        .toList()
                );
    }

    public FileObject getFileObject(Long userId, String path) {
        MinioPath minioPath = resolver.resolve(userId, path);
        return MinioExceptionHandler.interceptMinioExceptions(() -> {
            InputStream objectInputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(minioPath.getPathByMinio())
                    .build());
            return new FileObject(path, objectInputStream);
        });
    }

    public List<FileObject> getFileObjects(Long userId, String path) {
        MinioPath minioPath = resolver.resolve(userId, path);
        List<Item> minioItems = getMinioItems(minioPath, true).orElseThrow();
        return minioItems.stream()
                .map(item -> {
                    String objectName = item.objectName();
                    String objectPath = resolver.resolvePathFromMinioObjectName(userId, objectName);
                    InputStream objectInputStream = MinioExceptionHandler
                            .interceptMinioExceptions(() -> minioClient.getObject(GetObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(objectName)
                                    .build()));
                    return new FileObject(objectPath, objectInputStream);
                }).collect(Collectors.toList());
    }

    public void saveObject(Long userId, String path, MultipartFile file) {
        MinioPath minioPath = resolver.resolve(userId, path);
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

    public void copyObject(Long userId, String srcPath, String destPath) {
        MinioPath srcMinioPath = resolver.resolve(userId, srcPath);
        MinioPath destMinioPath = resolver.resolve(userId, destPath);
        MinioExceptionHandler.interceptMinioExceptions(() ->
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucket)
                                .object(destMinioPath.getPathByMinio())
                                .source(
                                        CopySource.builder()
                                                .bucket(bucket)
                                                .object(srcMinioPath.getPathByMinio())
                                                .build())
                                .build()));
    }

    public void deleteObjects(Long userId, String path) {
        log.info("Delete folder {} for user with id = {}", path, userId);
        MinioPath minioPath = resolver.resolve(userId, path);
        getMinioItems(minioPath, true)
                .ifPresent(items -> items
                        .forEach(item ->
                                MinioExceptionHandler.interceptMinioExceptions(() -> minioClient.removeObject(
                                        RemoveObjectArgs.builder().bucket(bucket)
                                                .object(item.objectName())
                                                .build()))));
        MinioExceptionHandler.interceptMinioExceptions(() -> minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucket)
                        .object(minioPath.getPathByMinio())
                        .build()));
    }

    private Optional<List<Item>> getMinioItems(MinioPath minioPath, boolean includeSubObjects) {
        Iterable<Result<Item>> minioItems = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .recursive(includeSubObjects)
                        .prefix(minioPath.getPathByMinio())
                        .build());

        if (!minioItems.iterator().hasNext()) {
            // If storage is empty. Root not contain objects. It is not an error.
            return Optional.ofNullable((minioPath.isRoot()) ? Collections.emptyList() : null);
        }
        return Optional.of(StreamSupport.stream(minioItems.spliterator(), false)
                .map(item -> MinioExceptionHandler.interceptMinioExceptions(item::get))
                .filter(item -> !minioPath.getPathByMinio().equals(item.objectName()))
                .collect(Collectors.toList()));
    }

    private Long getChildCount(MinioPath minioPath) {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Deprecated // Don't use 'file' or 'folder' on this layer.
    //TODO Move to service layer
    public void renameFolder(Long userId, String path, String newFolderName) {
        log.info("Rename folder {} for user with id = {}", path, userId);
        MinioPath minioPath = resolver.resolve(userId, path);
        MinioExceptionHandler.interceptMinioExceptions(() -> {
            List<Item> minioItems = getMinioItems(resolver.resolve(userId, path), true)
                    .orElseThrow();
            for (Item result : minioItems) {
                String sourceName = result.objectName();
                String destName = sourceName.replace(minioPath.getPathByMinio(),
                        minioPath.getParentMinioPath() + newFolderName + "/");
                copyObject(userId, sourceName, destName);
            }
        });
        deleteObjects(userId, path);
    }


}

