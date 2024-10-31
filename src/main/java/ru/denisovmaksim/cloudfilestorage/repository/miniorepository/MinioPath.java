package ru.denisovmaksim.cloudfilestorage.repository.miniorepository;


import lombok.Getter;

@Getter
class MinioPath {
    private final String userFolder;
    private final String pathByUser;
    private final String pathByMinio;

    MinioPath(Long userId, String path) {
        this.userFolder = String.format("user-%d-files/", userId);
        this.pathByUser = path;
        pathByMinio = userFolder + path;
    }
}
