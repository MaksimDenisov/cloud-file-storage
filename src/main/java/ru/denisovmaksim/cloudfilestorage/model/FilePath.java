package ru.denisovmaksim.cloudfilestorage.model;

import org.springframework.http.MediaType;
import ru.denisovmaksim.cloudfilestorage.exception.StoragePathException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FilePath extends StoragePath {
    private FilePath(String value) {
        super(value);
    }

    public static FilePath of(String path) {
        isValid(path);
        String normalized = normalizeFile(path);
        return new FilePath(normalized);
    }

    public DirPath parent() {
        return DirPath.of(parentDir());
    }

    public FilePath renameTo(String newName) {
        validateName(newName);
        DirPath parent = parent();
        return parent.resolveAsFile(newName);
    }

    public String detectMimeType() {
        try {
            String mimeType = Files.probeContentType(Path.of(value()));
            return mimeType != null ? mimeType : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    private static String normalizeFile(String path) {
        path = path.trim().replaceAll(SEPARATOR + "{2,}", SEPARATOR);
        if (path.isBlank()) {
            throw new StoragePathException("File path cannot be empty");
        }
        if (path.endsWith("/")) {
            throw new StoragePathException("File path cannot end with '/'");
        }
        return path;
    }
}
