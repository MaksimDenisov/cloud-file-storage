package ru.denisovmaksim.cloudfilestorage.util;

import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesUtil {
    public static String detectMimeType(String path) {
        try {
            return Files.probeContentType(Path.of(path));
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}
