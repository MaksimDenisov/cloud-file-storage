package ru.denisovmaksim.cloudfilestorage.model;

import org.springframework.http.MediaType;
import ru.denisovmaksim.cloudfilestorage.exception.RootFolderException;
import ru.denisovmaksim.cloudfilestorage.exception.StoragePathException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public final class StoragePath {
    public static final String AVAILABLE_CHARS = "( ) , - . ^ _ ` ! $ № + = @ &";
    private static final Pattern PATTERN =
            Pattern.compile("^([\\p{L}0-9/(),-.^_`!$№ +=@&])+$|^$");
    private static final String SEPARATOR = "/";
    private final String value;

    private StoragePath(String value) {
        this.value = value;
    }

    public static StoragePath dir(String path) {
        isValid(path);
        String normalized = normalizeDir(path);
        return new StoragePath(normalized);
    }

    public static StoragePath file(String path) {
        isValid(path);
        String normalized = normalizeFile(path);
        return new StoragePath(normalized);
    }

    public boolean isRoot() {
        return SEPARATOR.equals(value);
    }

    public boolean isDir() {
        return value.endsWith(SEPARATOR);
    }

    public boolean isFile() {
        return !isDir();
    }

    public String name() {
        if (isRoot()) {
            return "";
        }
        String trimmed = value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        int idx = trimmed.lastIndexOf('/');
        return idx >= 0 ? trimmed.substring(idx + 1) : trimmed;
    }

    public StoragePath parent() {
        if (isRoot()) {
            throw new RootFolderException("Root folder has no parent");
        }
        String trimmed = value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        int idx = trimmed.lastIndexOf('/');
        if (idx < 0) {
            return StoragePath.dir(SEPARATOR);
        }
        return new StoragePath(trimmed.substring(0, idx + 1));
    }

    public StoragePath renameTo(String newName) {
        if (isRoot()) {
            throw new RootFolderException("Root folder cannot be renamed");
        }

        validateName(newName);

        StoragePath parent = parent();
        if (isDir()) {
            return parent.resolve(newName).asDirectory();
        }
        return parent.resolve(newName);
    }

    public String detectMimeType() {
        if (isFile()) {
            try {
                return Files.probeContentType(Path.of(value));
            } catch (IOException e) {
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
        }
        throw new StoragePathException("MIME detection is only supported for files, not directories");
    }

    public StoragePath resolve(String child) {
        validateName(child);

        String base = isRoot() ? "" : value;
        return new StoragePath(base + child);
    }

    public StoragePath asDirectory() {
        if (value.endsWith("/")) {
            return this;
        }
        return new StoragePath(value + "/");
    }


    public String value() {
        return value;
    }

    private static void isValid(String path) {
        if (path == null) {
            throw new StoragePathException("The must be not null");
        }
        if (!path.isBlank() && !PATTERN.matcher(path).matches()) {
            String message = String.format("The must contains only : %s %s",
                    SEPARATOR,
                    AVAILABLE_CHARS);
            throw new StoragePathException(message);
        }
        if (path.contains("/../") || path.startsWith("../") || path.endsWith("/..")) {
            throw new StoragePathException("The must not contains ..");
        }
    }

    private static String normalizeDir(String path) {
        path = path.trim().replaceAll(SEPARATOR + "{2,}", SEPARATOR);
        return path.endsWith(SEPARATOR) ? path : path + SEPARATOR;
    }

    private static String normalizeFile(String path) {
        path = path.replaceAll(SEPARATOR + "{2,}", SEPARATOR);
        if (path.isBlank()) {
            throw new StoragePathException("File path cannot be empty");
        }
        if (path.endsWith("/")) {
            throw new StoragePathException("File path cannot end with '/'");
        }
        return path;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new StoragePathException("Name cannot be empty");
        }
        if (name.contains("/")) {
            throw new StoragePathException("Name must not contain '/'");
        }
    }
}
