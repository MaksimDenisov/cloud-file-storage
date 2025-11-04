package ru.denisovmaksim.cloudfilestorage.util;

import ru.denisovmaksim.cloudfilestorage.model.FileType;

import java.util.Set;

public final class FileTypeResolver {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp"
    );

    private FileTypeResolver() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static FileType detectFileType(String filepath) {
        String filename = PathUtil.getBaseName(filepath).toLowerCase();
        String extension = getExtension(filename);

        if (IMAGE_EXTENSIONS.contains(extension)) {
            return FileType.IMAGE;
        }

        if (filepath.endsWith(".mp3")) {
            return FileType.AUDIO;
        }

        return FileType.UNKNOWN_FILE;
    }

    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
