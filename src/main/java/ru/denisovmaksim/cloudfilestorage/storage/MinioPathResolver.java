package ru.denisovmaksim.cloudfilestorage.storage;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;

@Component
public class MinioPathResolver {
    MinioPath resolve(Long userId, String path) {
        path = path.replaceAll("/{2,}", "/");
        validatePath(path);
        return MinioPath.createValidated(getUserFolder(userId), path);
    }

    String resolvePathFromMinioObjectName(Long userId, String objectName) {
        return objectName.replace(getUserFolder(userId), "");
    }

    private String getUserFolder(Long userId) {
        return String.format("user-%d-files/", userId);
    }

    private void validatePath(String path) {
        int byteLength = path.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength > 1024) {
            throw new IllegalArgumentException(String.format("The path size %s exceed 1024 bytes", path));
        }
        Pattern pattern = Pattern.compile("[\\\\^*|&\";]|\\.{2}");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            throw new IllegalArgumentException(
                    String.format("The path %s must not contains  ^ * | \\\\ / & \\\" ; ..", path));
        }
    }
}
