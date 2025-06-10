package ru.denisovmaksim.cloudfilestorage.storage;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;

@Component
public class MinioPathResolver {

    private static final int MINIO_MAX_PREFIX_BYTE_LENGTH = 1024;
    private final Pattern pattern = Pattern.compile("[\\\\^*|&\";]|\\.{2}");

    String resolveMinioPath(Long userId, String path) {
        path = path.replaceAll("/{2,}", "/");
        int byteLength = path.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength > MINIO_MAX_PREFIX_BYTE_LENGTH) {
            throw new IllegalArgumentException(String.format("The path size %s exceed %d bytes",
                    path, MINIO_MAX_PREFIX_BYTE_LENGTH));
        }
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            throw new IllegalArgumentException(
                    String.format("The path %s must not contains  ^ * | \\\\ / & \\\" ; ..", path));
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
