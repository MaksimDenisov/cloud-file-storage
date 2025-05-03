package ru.denisovmaksim.cloudfilestorage.storage;


import lombok.Getter;

@Getter
final class MinioPath {
    private final String pathByUser;
    private final String userFolder;

    private MinioPath(String userFolder, String path) {
        this.pathByUser = path;
        this.userFolder = userFolder;
    }

    static MinioPath createValidated(String userFolder, String path) {
        return new MinioPath(userFolder, path);
    }

    boolean isRoot() {
        return getPathByMinio().equals(userFolder);
    }

    String getPathByMinio() {
        return userFolder + pathByUser;
    }
}
