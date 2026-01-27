package ru.denisovmaksim.cloudfilestorage.storage;

import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

@Component
@RequiredArgsConstructor
public class StorageObjectInfoMapper {
    private final MinioPathResolver resolver;

    StorageObjectInfo from(Long userId, StatObjectResponse stat) {
        String objectPath = resolver.resolvePathFromMinioObjectName(userId, stat.object());
        String baseName = PathUtil.getBaseName(objectPath);
        boolean isDir = PathUtil.isDir(objectPath);
        return new StorageObjectInfo(objectPath, baseName, isDir, stat.size());
    }

    StorageObjectInfo from(Long userId, Item item) {
        String objectPath = resolver.resolvePathFromMinioObjectName(userId, item.objectName());
        String baseName = PathUtil.getBaseName(objectPath);
        boolean isDir = PathUtil.isDir(objectPath);
        return new StorageObjectInfo(objectPath, baseName, isDir, item.size());
    }
}
