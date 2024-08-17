package ru.denisovmaksim.cloudfilestorage.repository.miniorepository;


import lombok.Getter;

@Getter
class MinioPath {
    private final String userFolder;
    private final String path;
    private final String fullMinioPath;

    MinioPath(Long userId, String path) {
        this.userFolder = String.format("user-%d-files/", userId);
        this.path = path;
        fullMinioPath = userFolder + path;
    }
}
