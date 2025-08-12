package ru.denisovmaksim.cloudfilestorage.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class MinioPathResolver {

    private static final int MINIO_MAX_PREFIX_BYTE_LENGTH = 1024;

    String resolveMinioPath(Long userId, String path) {
        path = path.replaceAll("/{2,}", "/");
        int byteLength = path.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength > MINIO_MAX_PREFIX_BYTE_LENGTH) {
            String message = String.format("The path size %s exceed %d bytes",
                    path, MINIO_MAX_PREFIX_BYTE_LENGTH);
            log.error(message);
            throw new IllegalArgumentException(message);
        }
        if (!PathUtil.isValid(path)) {
            String message = String.format("The must contains only : %s %s",
                    PathUtil.PATH_SEPARATOR,
                    PathUtil.AVAILABLE_CHARS);
            throw new IllegalArgumentException(message);
        }
        return getUserFolder(userId) + path;
    }

    String resolvePathFromMinioObjectName(Long userId, String objectName) {
        return objectName.replace(getUserFolder(userId), "");
    }

    private String getUserFolder(Long userId) {
        return String.format("user-%d-files/", userId);
    }
}
