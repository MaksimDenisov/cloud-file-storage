package ru.denisovmaksim.cloudfilestorage.repository.miniorepository;

import io.minio.messages.Item;
import lombok.Getter;
import ru.denisovmaksim.cloudfilestorage.model.StorageObjectType;

import java.time.ZonedDateTime;


final class MinioItemDescription {
    private final MinioPath minioPath;
    @Getter
    private final String minioName;
    @Getter
    private final ZonedDateTime lastModified;
    @Getter
    private final long size;
    private final String[] elements;

    MinioItemDescription(MinioPath minioPath, Item result) {
        this.minioPath = minioPath;
        minioName = result.objectName();
        lastModified = result.lastModified();
        size = result.size();
        elements = minioName.replace(minioPath.getFullMinioPath(), "").split("/");
    }

    public String getDirectElementName() {
        return elements[0];
    }

    public String getDirectElementPath() {
        return minioPath.getPath() + elements[0]
                + ((getType() == StorageObjectType.FOLDER) ? "/" : "");
    }

    public boolean isRootFolder() {
        return elements[0].isEmpty();
    }

    public StorageObjectType getType() {
        return (minioName.endsWith("/")) ? StorageObjectType.FOLDER : StorageObjectType.UNKNOWN_FILE;
    }

    public boolean hasOnlyOneChild() {
        return elements.length == 2;
    }
}
