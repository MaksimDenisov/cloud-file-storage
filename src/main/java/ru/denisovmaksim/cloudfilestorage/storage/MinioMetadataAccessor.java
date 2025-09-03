package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Component responsible for retrieving object metadata with MinIO storage.
 */
@Component
@Slf4j
public class MinioMetadataAccessor {
    private final MinioClient minioClient;
    private final MinioPathResolver resolver;
    private final String bucket;
    private final MinioObjectFetcher objectFetcher;

    public MinioMetadataAccessor(MinioClient minioClient,
                                 MinioPathResolver resolver,
                                 MinioObjectFetcher objectFetcher,
                                 @Value("${app.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.resolver = resolver;
        this.bucket = bucket;
        this.objectFetcher = objectFetcher;
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
     * Gets the size of an object in MinIO for the given user and path.
     *
     * @param userId user identifier
     * @param path   relative object path
     * @return object size in bytes, or {@code -1} if the object does not exist
     */
    public long getSize(Long userId, String path) {
        log.info("Check exist path '{}' for userId={}", path, userId);
        if (!isExist(userId, path)) {
            return -1;
        }
        return MinioExceptionHandler.interceptMinioExceptions(() ->
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucket)
                                .object(resolver.resolveMinioPath(userId, path))
                                .build()).size());
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
        return objectFetcher.getMinioItems(userId, path, false)
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
        List<Item> items = objectFetcher.getMinioItems(userId, path, true)
                .orElseGet(Collections::emptyList);
        final String upperCaseQuery = query.toUpperCase();
        return items.stream()
                .filter(name -> resolver.resolvePathFromMinioObjectName(userId, name.objectName()).toUpperCase()
                        .contains(upperCaseQuery))
                .map(item -> toStorageObjectInfo(userId, item))
                .toList();
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

