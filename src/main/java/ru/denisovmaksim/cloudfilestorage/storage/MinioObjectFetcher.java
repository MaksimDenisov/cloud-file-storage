package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Component
public class MinioObjectFetcher {

    private final MinioClient minioClient;
    private final MinioPathResolver resolver;
    private final String bucket;

    MinioObjectFetcher(MinioClient minioClient,
                       MinioPathResolver resolver,
                       @Value("${app.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.resolver = resolver;
        this.bucket = bucket;
    }

    /**
     * Retrieves raw MinIO items from storage for the given path.
     *
     * @param userId            the ID of the user
     * @param path              path representation
     * @param includeSubObjects whether to include nested objects
     * @return a list of MinIO items or empty if path is empty
     */
    protected Optional<List<Item>> getMinioItems(Long userId, String path, boolean includeSubObjects) {
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
                .toList());
    }
}
