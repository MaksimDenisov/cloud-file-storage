package ru.denisovmaksim.cloudfilestorage.util;

import ru.denisovmaksim.cloudfilestorage.model.FileType;

public final class FileTypeResolver {
    private FileTypeResolver() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static FileType detectFileType(String filepath) {
        filepath = filepath.toLowerCase();
        if (filepath.endsWith(".mp3")) {
            return FileType.MUSIC;
        }
        return FileType.UNKNOWN_FILE;
    }
}
