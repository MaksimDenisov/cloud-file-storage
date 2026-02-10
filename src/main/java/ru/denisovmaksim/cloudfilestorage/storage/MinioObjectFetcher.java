package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.config.MinioProperties;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
class MinioObjectFetcher {

    private final MinioClient minioClient;
    private final MinioPathResolver resolver;
    private final MinioProperties properties;


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
                        .bucket(properties.bucket())
                        .recursive(includeSubObjects)
                        .prefix(minioPath)
                        .build());

        if (!minioItems.iterator().hasNext()) {
            // If storage is empty. Root not contain objects. It is not an error.
            return Optional.ofNullable((PathUtil.isRoot(path)) ? Collections.emptyList() : null);
        }
        return Optional.of(StreamSupport.stream(minioItems.spliterator(), false)
                .map(item -> MinioExceptionHandler.callWithMinio(item::get))
                .filter(item -> !minioPath.equals(item.objectName()))
                .toList());
    }
}
