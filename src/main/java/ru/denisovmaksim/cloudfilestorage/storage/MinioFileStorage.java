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
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Component responsible for interacting with MinIO storage.
 * Provides functionality for uploading, retrieving, copying, and deleting objects.
 */
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

    /**
     * Checks whether at least one object exists in the specified path for the given user.
     *
     * @param userId the ID of the user whose storage space is being checked
     * @param path   the virtual path to check for object existence
     * @return {@code true} if at least one object exists at the specified path; {@code false} otherwise
     */
    public boolean isExist(Long userId, String path) {
        log.info("Check exist path '{}' for userId={}", path, userId);
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(resolver.resolveMinioPath(userId, path))
                        .maxKeys(1)
                        .build()
        );
        return results.iterator().hasNext();
    }

    /**
     * Creates an empty folder object in the storage for the specified user and path.
     *
     * @param userId the ID of the user
     * @param path   the logical path to be created
     */
    public void createPath(Long userId, String path) {
        log.info("Create path '{}' for userId={}", path, userId);
        MinioExceptionHandler.interceptMinioExceptions(() -> {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(resolver.resolveMinioPath(userId, path))
                                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                                    .build());
                }
        );
    }

    /**
     * Retrieves metadata about the objects (files and folders) at the given path.
     *
     * @param userId the ID of the user
     * @param path   the logical user path
     * @return a list of storage object information if present
     */
    public Optional<List<StorageObjectInfo>> listObjectInfo(Long userId, String path) {
        log.info("Fetching objects info at path '{}' for userId={}", path, userId);
        return getMinioItems(userId, path, false)
                .map(list -> list.stream()
                        .map(item -> toStorageObjectInfo(userId, item))
                        .toList());
    }

    /**
     * Retrieves metadata about the objects (files and folders) containing query substring in path.
     *
     * @param userId the ID of the user
     * @param query  Searching substring
     * @return a list of storage object information if present
     */
    public List<StorageObjectInfo> searchObjectInfo(Long userId, String path, String query) {
        log.info("Searching objects info at path '{}' for userId={}", path, userId);
        List<Item> items = getMinioItems(userId, path, true)
                .orElseGet(Collections::emptyList);
        final String upperCaseQuery = query.toUpperCase();
        return items.stream()
                .filter(name -> resolver.resolvePathFromMinioObjectName(userId, name.objectName()).toUpperCase()
                        .contains(upperCaseQuery))
                .map(item -> toStorageObjectInfo(userId, item))
                .toList();
    }

    /**
     * Downloads a single object from storage.
     *
     * @param userId the ID of the user
     * @param path   the path to the object
     * @return the object along with its data stream
     */
    public FileObject getObject(Long userId, String path) {
        log.info("Downloading object at path '{}' for userId={}", path, userId);
        return MinioExceptionHandler.interceptMinioExceptions(() -> {
            InputStream objectInputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(resolver.resolveMinioPath(userId, path))
                    .build());
            return new FileObject(path, objectInputStream);
        });
    }

    /**
     * Downloads all objects (recursively) under the specified path.
     *
     * @param userId the ID of the user
     * @param path   the folder path
     * @return a list of file objects with their data streams
     */
    public List<FileObject> getObjects(Long userId, String path) {
        List<Item> minioItems = getMinioItems(userId, path, true).orElseThrow();
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

    /**
     * Uploads a file to the specified path.
     *
     * @param userId the ID of the user
     * @param path   the folder path where the file will be stored
     * @param file   the multipart file to upload
     */
    public void saveObject(Long userId, String path, MultipartFile file) {
        log.info("Uploading file '{}' to path '{}' for userId={}", file.getOriginalFilename(), path, userId);
        MinioExceptionHandler.interceptMinioExceptions(() -> {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(resolver.resolveMinioPath(userId, path) + file.getOriginalFilename())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        });
    }

    /**
     * Copies a single object from one path to another.
     *
     * @param userId   the ID of the user
     * @param srcPath  the source path
     * @param destPath the destination path
     */
    public void copyOneObject(Long userId, String srcPath, String destPath) {
        log.info("Copying one object from path '{}' to path '{}' for userId={}", srcPath, destPath, userId);
        MinioExceptionHandler.interceptMinioExceptions(() ->
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucket)
                                .object(resolver.resolveMinioPath(userId, destPath))
                                .source(
                                        CopySource.builder()
                                                .bucket(bucket)
                                                .object(resolver.resolveMinioPath(userId, srcPath))
                                                .build())
                                .build()));
    }

    /**
     * Copies all objects (recursively) from one directory to another.
     *
     * @param userId   the ID of the user
     * @param srcPath  the source directory
     * @param destPath the target directory
     */
    public int copyObjects(Long userId, String srcPath, String destPath) {
        log.info("Copying all objects from path '{}' to path '{}' for userId={}", srcPath, destPath, userId);
        String srcMinioPath = resolver.resolveMinioPath(userId, srcPath);
        AtomicInteger count = new AtomicInteger();
        MinioExceptionHandler.interceptMinioExceptions(() -> {
            List<Item> minioItems = getMinioItems(userId, srcPath, true)
                    .orElseThrow();
            for (Item item : minioItems) {
                String fromPath = resolver.resolvePathFromMinioObjectName(userId, item.objectName());
                String toPath = item.objectName()
                        .replaceFirst(srcMinioPath, destPath);
                count.incrementAndGet();
                copyOneObject(userId, fromPath, toPath);
            }
        });
        return count.get();
    }

    /**
     * Deletes all objects under the specified path, including the folder itself.
     *
     * @param userId the ID of the user
     * @param path   the folder path to delete
     */
    public void deleteObjects(Long userId, String path) {
        log.info("Deleting all objects at path '{}' for userId={}", path, userId);
        getMinioItems(userId, path, true)
                .ifPresent(items -> items
                        .forEach(item ->
                                MinioExceptionHandler.interceptMinioExceptions(() -> minioClient.removeObject(
                                        RemoveObjectArgs.builder().bucket(bucket)
                                                .object(item.objectName())
                                                .build()))));
        MinioExceptionHandler.interceptMinioExceptions(() -> minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucket)
                        .object(resolver.resolveMinioPath(userId, path))
                        .build()));
    }

    /**
     * Returns the number of direct children for a given path.
     *
     * @param userId the ID of the user
     * @param path   the folder path to delete
     * @return the count of child items
     */
    public Long getDirectChildCount(Long userId, String path) {
        log.info("Get count direct child at path '{}' for userId={}", path, userId);
        String minioPath = resolver.resolveMinioPath(userId, path);
        Iterable<Result<Item>> minioItems = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(minioPath)
                        .build());
        return StreamSupport.stream(minioItems.spliterator(), false)
                .map(item -> MinioExceptionHandler.interceptMinioExceptions(item::get))
                .filter(item -> !item.objectName().equals(minioPath))
                .count();
    }


    //TODO Add commonSize

    /**
     * Retrieves raw MinIO items from storage for the given path.
     *
     * @param userId            the ID of the user
     * @param path              path representation
     * @param includeSubObjects whether to include nested objects
     * @return a list of MinIO items or empty if path is empty
     */
    private Optional<List<Item>> getMinioItems(Long userId, String path, boolean includeSubObjects) {
        String minioPath = resolver.resolveMinioPath(userId, path);
        Iterable<Result<Item>> minioItems = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .recursive(includeSubObjects)
                        .prefix(minioPath)
                        .build());

        if (!minioItems.iterator().hasNext()) {
            // If storage is empty. Root not contain objects. It is not an error.
            return Optional.ofNullable((PathUtil.isRoot(path)) ? Collections.emptyList() : null);
        }
        return Optional.of(StreamSupport.stream(minioItems.spliterator(), false)
                .map(item -> MinioExceptionHandler.interceptMinioExceptions(item::get))
                .filter(item -> !minioPath.equals(item.objectName()))
                .collect(Collectors.toList()));
    }

    /**
     * Map raw MinIO item to StorageObjectInfo
     *
     * @param userId the ID of the user
     * @param item   raw MinIO item from storage
     * @return a StorageObjectInfo object
     */
    private StorageObjectInfo toStorageObjectInfo(Long userId, Item item) {
        String objectPath = resolver.resolvePathFromMinioObjectName(userId, item.objectName());
        String baseName = PathUtil.getBaseName(objectPath);
        boolean isDir = PathUtil.isDir(objectPath);
        return new StorageObjectInfo(objectPath, baseName, isDir, item.size());
    }
}

