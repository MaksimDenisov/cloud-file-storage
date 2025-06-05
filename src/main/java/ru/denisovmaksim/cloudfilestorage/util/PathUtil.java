package ru.denisovmaksim.cloudfilestorage.util;

import java.util.regex.Pattern;

public class PathUtil {

    private static final Pattern PATTERN =
            Pattern.compile("^([\\p{L}0-9/._\\-, ])+$|^$");

    public static String normalize(String path) {
        return path.trim()
                .replaceAll("/{2,}", "/");
    }

    public static boolean isRoot(String path) {
        return "".equals(path) || "/".equals(path);
    }

    public static boolean isValid(String path) {
        return path != null
                && PATTERN.matcher(path).matches();
    }

    public static boolean isDir(String path) {
        return path.endsWith("/");
    }

    public static String getBaseName(String path) {
        if (path.endsWith("/")) {
            return path.substring(path.lastIndexOf('/', path.length() - 2) + 1, path.length() - 1);
        } else {
            return path.substring(path.lastIndexOf('/') + 1);
        }
    }

    public static String getParentDirName(String path) {
        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlashIndex = path.lastIndexOf('/');
        return (lastSlashIndex == -1) ? "" : path.substring(0, lastSlashIndex) + "/";
    }
}
