package ru.denisovmaksim.cloudfilestorage.util;

public class FilePathUtil {

    public static String getParentPath(String path) {
        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlashIndex = path.lastIndexOf('/');
        return (lastSlashIndex == -1) ? "" : path.substring(0, lastSlashIndex) + "/";
    }
}
