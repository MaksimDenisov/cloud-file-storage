package ru.denisovmaksim.cloudfilestorage.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

import static ru.denisovmaksim.cloudfilestorage.storage.MinioExceptionHandler.callWithMinio;
import static ru.denisovmaksim.cloudfilestorage.storage.MinioExceptionHandler.runWithMinio;

/**
 * Component responsible for interacting with MinIO storage.
 * Provides functionality for uploading, retrieving, copying, and deleting objects.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StorageDataAccessor implements DataAccessor {
    private final MinioDataAccessor dataAccessor;

    @Override
    public StorageObject getObject(Long userId, String path) {
        log.info("Downloading object at path '{}' for userId={}", path, userId);
        return callWithMinio(() -> dataAccessor.getObject(userId, path));
    }

    @Override
    public InputStream getRangeOfObject(Long userId, String path, Long rangeStart, Long contentLength) {
        return callWithMinio(() -> dataAccessor.getRangeOfObject(userId, path, rangeStart, contentLength));
    }

    @Override
    public List<StorageObject> getObjects(Long userId, String path) {
        return callWithMinio(() -> dataAccessor.getObjects(userId, path));
    }

    @Override
    public void saveObject(Long userId, String path, MultipartFile file) {
        log.info("Uploading file '{}' to path '{}' for userId={}", file.getOriginalFilename(), path, userId);
        runWithMinio(() -> dataAccessor.saveObject(userId, path, file));
    }

    @Override
    public void copyOneObject(Long userId, String srcPath, String destPath) {
        log.info("Copying one object from path '{}' to path '{}' for userId={}", srcPath, destPath, userId);
        runWithMinio(() -> dataAccessor.copyOneObject(userId, srcPath, destPath));
    }

    @Override
    public int copyObjects(Long userId, String srcPath, String destPath) {
        log.info("Copying all objects from path '{}' to path '{}' for userId={}", srcPath, destPath, userId);
        return callWithMinio(() -> dataAccessor.copyObjects(userId, srcPath, destPath));
    }

    @Override
    public void deleteOneObject(Long userId, String path) {
        log.info("Deleting one object at path '{}' for userId={}", path, userId);
        runWithMinio(() -> dataAccessor.deleteOneObject(userId, path));
    }

    @Override
    public void deleteObjects(Long userId, String path) {
        log.info("Deleting all objects at path '{}' for userId={}", path, userId);
        runWithMinio(() -> dataAccessor.deleteObjects(userId, path));
    }
}
