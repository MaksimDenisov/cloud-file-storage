package ru.denisovmaksim.cloudfilestorage.model;

import ru.denisovmaksim.cloudfilestorage.exception.RootFolderException;

public final class DirPath extends StoragePath {
    private DirPath(String value) {
        super(value);
    }

    public static DirPath of(String path) {
        isValid(path);
        String normalized = normalizeDir(path);
        return new DirPath(normalized);
    }
    public DirPath parent() {
        if (isRoot()) {
            throw new RootFolderException("Root folder has no parent");
        }
        return new DirPath(parentDir());
    }

    public DirPath resolveAsDir(String child) {
        return new DirPath(resolve(child) + SEPARATOR);
    }

    public FilePath resolveAsFile(String child) {
        return FilePath.of(resolve(child));
    }

    public DirPath renameTo(String newName) {
        if (isRoot()) {
            throw new RootFolderException("Root folder cannot be renamed");
        }
        validateName(newName);
        DirPath parent = parent();
        return parent.resolveAsDir(newName);
    }

    private static String normalizeDir(String path) {
        path = path.trim().replaceAll(SEPARATOR + "{2,}", SEPARATOR);
        return path.endsWith(SEPARATOR) ? path : path + SEPARATOR;
    }


}
