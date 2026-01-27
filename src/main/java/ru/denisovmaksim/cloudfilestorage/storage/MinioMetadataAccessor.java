package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.config.MinioProperties;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Component responsible for retrieving object metadata with MinIO storage.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MinioMetadataAccessor {
    private final MinioClient minioClient;
    private final MinioPathResolver resolver;
    private final MinioObjectFetcher objectFetcher;
    private final StorageObjectInfoMapper mapper;
    private final MinioProperties properties;

    /**
     * Creates an empty folder object in the storage for the specified user and path.
     *
     * @param userId the ID of the user
     * @param path   the logical path to be created
     */
    public void createPath(Long userId, String path) {
        log.info("Create path '{}' for userId={}", path, userId);
        MinioExceptionHandler.interceptMinioExceptions(() ->
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(properties.bucket())
                                .object(resolver.resolveMinioPath(userId, path))
                                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                                .build())
        );
    }

    /**
     * Checks whether at least one object exists in the specified path for the given user.
     *
     * @param userId the ID of the user whose storage space is being checked
     * @param path   the virtual path to check for object existence
     * @return {@code true} if at least one object exists at the specified path; {@code false} otherwise
     */
    public boolean isExistByPrefix(Long userId, String path) {
        log.info("Check exist path '{}' for userId={}", path, userId);
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(properties.bucket())
                        .prefix(resolver.resolveMinioPath(userId, path))
                        .maxKeys(1)
                        .build()
        );
        return results.iterator().hasNext();
    }

    public Optional<StorageObjectInfo> getOne(Long userId, String path) {
        return MinioExceptionHandler.interceptMinioExceptions(() -> {
            if (!isExistByPrefix(userId, path)) {
                return Optional.empty();
            }
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.bucket())
                            .object(resolver.resolveMinioPath(userId, path))
                            .build());
            return Optional.of(mapper.from(userId, stat));
        });
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
                        .map(item -> mapper.from(userId, item))
                        .toList());
    }

    /**
     * Retrieves metadata about the objects (files and folders) containing query substring in path.
     *
     * @param userId the ID of the user
     * @param query  Searching substring
     * @return a list of storage object information if present
     */
    public List<StorageObjectInfo> findObjectInfosBySubstring(Long userId, String path, String query) {
        log.info("Searching objects info at path '{}' for userId={}", path, userId);
        List<Item> items = objectFetcher.getMinioItems(userId, path, true)
                .orElseGet(Collections::emptyList);
        final String upperCaseQuery = query.toUpperCase();
        return items.stream()
                .filter(name -> resolver.resolvePathFromMinioObjectName(userId, name.objectName()).toUpperCase()
                        .contains(upperCaseQuery))
                .map(item -> mapper.from(userId, item))
                .toList();
    }

    /**
     * Returns the number of direct children for a given path.
     *
     * @param userId the ID of the user
     * @param path   the logical user path
     * @return the count of child items
     */
    public Long getDirectChildCount(Long userId, String path) {
        log.info("Get count direct child at path '{}' for userId={}", path, userId);
        String minioPath = resolver.resolveMinioPath(userId, path);
        Iterable<Result<Item>> minioItems = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(properties.bucket())
                        .prefix(minioPath)
                        .build());
        return StreamSupport.stream(minioItems.spliterator(), false)
                .map(item -> MinioExceptionHandler.interceptMinioExceptions(item::get))
                .filter(item -> !item.objectName().equals(minioPath))
                .count();
    }
}
