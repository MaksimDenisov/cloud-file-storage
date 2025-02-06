package ru.denisovmaksim.cloudfilestorage.storage;


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

    public boolean isRoot() {
        return getPathByMinio().equals(userFolder);
    }

    public String getParentMinioPath() {
        String[] elements = pathByUser.split("/");
        StringBuilder builder = new StringBuilder(userFolder);
        for (int i = 0; i < elements.length - 1; i++) {
            builder.append(elements[i])
                    .append("/");
        }
        return builder.toString();
    }
}
