package ru.denisovmaksim.cloudfilestorage.model;

import ru.denisovmaksim.cloudfilestorage.exception.StoragePathException;

import java.util.Objects;
import java.util.regex.Pattern;

sealed class StoragePath permits DirPath, FilePath {
    public static final String AVAILABLE_CHARS = "( ) , - . ^ _ ` ! $ № + = @ &";
    private static final Pattern PATTERN =
            Pattern.compile("^([\\p{L}0-9/(),-.^_`!$№ +=@&])+$|^$");
    protected static final String SEPARATOR = "/";
    private final String value;

    protected StoragePath(String value) {
        this.value = value;
    }

    public boolean isRoot() {
        return SEPARATOR.equals(value());
    }

    public String name() {
        if (isRoot()) {
            return "";
        }
        String trimmed = value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        int idx = trimmed.lastIndexOf('/');
        return idx >= 0 ? trimmed.substring(idx + 1) : trimmed;
    }

    protected String parentDir() {
        String trimmed = value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        int idx = trimmed.lastIndexOf('/');
        return (idx < 0) ? SEPARATOR : trimmed.substring(0, idx + 1);
    }

    protected String resolve(String child) {
        validateName(child);
        String base = isRoot() ? "" : value;
        return base + child;
    }

    public String value() {
        return value;
    }

    protected static void isValid(String path) {
        if (path == null) {
            throw new StoragePathException("Path must be not null");
        }
        if (!path.isBlank() && !PATTERN.matcher(path).matches()) {
            String message = String.format("Path must contains only : %s %s",
                    SEPARATOR,
                    AVAILABLE_CHARS);
            throw new StoragePathException(message);
        }
        if (path.contains("/../") || path.startsWith("../") || path.endsWith("/..")) {
            throw new StoragePathException("Path must not contains ..");
        }
    }

    protected static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new StoragePathException("Name cannot be empty");
        }
        if (name.contains("/")) {
            throw new StoragePathException("Name must not contain '/'");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StoragePath that = (StoragePath) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
