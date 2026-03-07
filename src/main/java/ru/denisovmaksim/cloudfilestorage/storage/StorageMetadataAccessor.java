package ru.denisovmaksim.cloudfilestorage.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static ru.denisovmaksim.cloudfilestorage.storage.MinioExceptionHandler.callWithMinio;
import static ru.denisovmaksim.cloudfilestorage.storage.MinioExceptionHandler.runWithMinio;

@Component
@Slf4j
@RequiredArgsConstructor
public class StorageMetadataAccessor implements MetadataAccessor {
    private final MinioMetadataAccessor minioMetadataAccessor;

    @Override
    public void createPath(Long userId, String path) {
        runWithMinio(() -> minioMetadataAccessor.createPath(userId, path));
    }

    @Override
    public Optional<StorageObjectInfo> getOne(Long userId, String path) {
        return callWithMinio(() -> minioMetadataAccessor.getOne(userId, path));
    }

    @Override
    public Optional<List<StorageObjectInfo>> listObjectInfo(Long userId, String path) {
        return minioMetadataAccessor.listObjectInfo(userId, path);
    }

    @Override
    public List<StorageObjectInfo> findObjectInfosBySubstring(Long userId, String path, String query) {
        return minioMetadataAccessor.findObjectInfosBySubstring(userId, path, query);
    }
    @Override
    public boolean exist(Long userId, String path) {
        return minioMetadataAccessor.exist(userId, path);
    }
}
