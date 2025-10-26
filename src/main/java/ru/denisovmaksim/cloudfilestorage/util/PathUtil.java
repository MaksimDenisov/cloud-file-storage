package ru.denisovmaksim.cloudfilestorage.util;

import java.util.regex.Pattern;

public final class PathUtil {

    public static final String PATH_SEPARATOR = "/";
    public static final String AVAILABLE_CHARS = "( ) , - . ^ _ ` ! $ № + = @ &";

    private PathUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final Pattern PATTERN =
            Pattern.compile("^([\\p{L}0-9/(),-.^_`!$№ +=@&])+$|^$");

    public static String normalize(String path) {
        return path.trim()
                .replaceAll("/{2,}", PATH_SEPARATOR);
    }

    public static boolean isRoot(String path) {
        return path.isEmpty() || PATH_SEPARATOR.equals(path);
    }

    public static boolean isValid(String path) {
        return path != null
                && PATTERN.matcher(path).matches()
                && !(path.contains("/../") || path.startsWith("../") || path.endsWith("/.."));
    }

    public static boolean isDir(String path) {
        return path.endsWith(PATH_SEPARATOR) || path.isEmpty();
    }


    public static String ensureDirectoryPath(String path) {
        if (!PathUtil.isDir(path)) {
            path = path + PathUtil.PATH_SEPARATOR;
        }
        return path;
    }


    public static String getBaseName(String path) {
        if (path.endsWith(PATH_SEPARATOR)) {
            return path.substring(path.lastIndexOf(PATH_SEPARATOR, path.length() - 2) + 1, path.length() - 1);
        } else {
            return path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1);
        }
    }

    public static String getParentPath(String path) {
        path = path.endsWith(PATH_SEPARATOR) ? path.substring(0, path.length() - 1) : path;
        int lastSlashIndex = path.lastIndexOf(PATH_SEPARATOR);
        return (lastSlashIndex == -1) ? "" : path.substring(0, lastSlashIndex) + PATH_SEPARATOR;
    }
}
