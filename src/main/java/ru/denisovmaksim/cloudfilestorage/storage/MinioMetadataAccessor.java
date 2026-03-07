package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.config.MinioProperties;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
class MinioMetadataAccessor {
    private final MinioClient minioClient;
    private final MinioPathResolver resolver;
    private final MinioObjectFetcher objectFetcher;
    private final StorageObjectInfoMapper mapper;
    private final MinioProperties properties;

    public void createPath(Long userId, String path) throws MinioException, IOException,
            NoSuchAlgorithmException, InvalidKeyException {
        log.info("Create path '{}' for userId={}", path, userId);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(properties.bucket())
                        .object(resolver.resolveMinioPath(userId, path))
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .build());

    }

    public Optional<StorageObjectInfo> getOne(Long userId, String path) throws MinioException, IOException,
            NoSuchAlgorithmException, InvalidKeyException {
        if (!isExistByPrefix(userId, path)) {
            return Optional.empty();
        }
        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(properties.bucket())
                        .object(resolver.resolveMinioPath(userId, path))
                        .build());
        return Optional.of(mapper.from(userId, stat));
    }

    public Optional<List<StorageObjectInfo>> listObjectInfo(Long userId, String path) {
        log.info("Fetching objects info at path '{}' for userId={}", path, userId);
        return objectFetcher.getMinioItems(userId, path, false)
                .map(list -> list.stream()
                        .map(item -> mapper.from(userId, item))
                        .toList());
    }

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

    public boolean exist(Long userId, String path) {
        if (PathUtil.isDir(path)) {
            return isExistByPrefix(userId, path);
        }
        return isExistByEquals(userId, path);
    }

    public boolean isExistByPrefix(Long userId, String path) {
        log.info("Check exist path by prefix '{}' for userId={}", path, userId);
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(properties.bucket())
                        .prefix(resolver.resolveMinioPath(userId, path))
                        .maxKeys(1)
                        .build()
        );
        return results.iterator().hasNext();
    }

    public boolean isExistByEquals(Long userId, String path) {
        log.info("Check exist path '{}' for userId={}", path, userId);
        String minioPath = resolver.resolveMinioPath(userId, path);
        Iterable<Result<Item>> minioItems = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(properties.bucket())
                        .prefix(minioPath)
                        .build());

        return StreamSupport.stream(minioItems.spliterator(), false)
                .map(item -> MinioExceptionHandler.callWithMinio(item::get))
                .anyMatch(item -> minioPath.equals(item.objectName()));
    }
}
