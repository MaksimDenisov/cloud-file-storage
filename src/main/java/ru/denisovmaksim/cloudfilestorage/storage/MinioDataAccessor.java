package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Component responsible for interacting with MinIO storage.
 * Provides functionality for uploading, retrieving, copying, and deleting objects.
 */
@Component
@Slf4j
public class MinioDataAccessor {
    private final MinioClient minioClient;
    private final MinioPathResolver resolver;
    private final String bucket;
    private final MinioObjectFetcher objectFetcher;

    public MinioDataAccessor(MinioClient minioClient, MinioPathResolver resolver,
                             MinioObjectFetcher objectFetcher, @Value("${app.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.resolver = resolver;
        this.bucket = bucket;
        this.objectFetcher = objectFetcher;
    }

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
                                .bucket(bucket)
                                .object(resolver.resolveMinioPath(userId, path))
                                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                                .build())
        );
    }

    /**
     * Downloads a single object from storage.
     *
     * @param userId the ID of the user
     * @param path   the path to the object
     * @return the object along with its data stream
     */
    public StorageObject getObject(Long userId, String path) {
        log.info("Downloading object at path '{}' for userId={}", path, userId);
        return MinioExceptionHandler.interceptMinioExceptions(() -> {
            String minioPath = resolver.resolveMinioPath(userId, path);
            InputStream objectInputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(minioPath)
                    .build());
            return new StorageObject(path, objectInputStream);
        });
    }

    /**
     * Retrieves a byte range of an object from MinIO as an input stream.
     *
     * @param userId        user identifier
     * @param path          relative object path
     * @param rangeStart    starting byte offset
     * @param contentLength number of bytes to read
     * @return input stream of the requested object range
     */
    public InputStream getRangeOfObject(Long userId, String path, Long rangeStart, Long contentLength) {
        String minioPath = resolver.resolveMinioPath(userId, path);
        return MinioExceptionHandler.interceptMinioExceptions(() -> minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(minioPath)
                        .offset(rangeStart)
                        .length(contentLength)
                        .build()));
    }


    /**
     * Downloads all objects (recursively) under the specified path.
     *
     * @param userId the ID of the user
     * @param path   the folder path
     * @return a list of file objects with their data streams
     */
    public List<StorageObject> getObjects(Long userId, String path) {
        List<Item> minioItems = objectFetcher.getMinioItems(userId, path, true).orElseThrow();
        List<StorageObject> fileObjects = new ArrayList<>();
        for (Item item : minioItems) {
            String objectName = item.objectName();
            String objectPath = resolver.resolvePathFromMinioObjectName(userId, objectName);
            InputStream objectInputStream = MinioExceptionHandler
                    .interceptMinioExceptions(() -> minioClient.getObject(GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()));
            fileObjects.add(new StorageObject(objectPath, objectInputStream));
        }
        return fileObjects;
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
        MinioExceptionHandler.interceptMinioExceptions(() ->
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(resolver.resolveMinioPath(userId, path + file.getOriginalFilename()))
                                .stream(file.getInputStream(), file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                )
        );
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
            List<Item> minioItems = objectFetcher.getMinioItems(userId, srcPath, true)
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
        objectFetcher.getMinioItems(userId, path, true)
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
}
