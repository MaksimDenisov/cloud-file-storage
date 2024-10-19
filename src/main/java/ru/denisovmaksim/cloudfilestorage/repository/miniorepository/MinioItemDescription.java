package ru.denisovmaksim.cloudfilestorage.repository.miniorepository;

import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.Getter;
import ru.denisovmaksim.cloudfilestorage.exceptions.FileStorageException;
import ru.denisovmaksim.cloudfilestorage.model.StorageObjectType;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;


final class MinioItemDescription {
    public static MinioItemDescription create(MinioPath minioPath, Result<Item> result) {
        try {
            return new MinioItemDescription(minioPath, result.get());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new FileStorageException(e);
        }
    }

    private final MinioPath minioPath;
    @Getter
    private final String minioName;
    @Getter
    private final ZonedDateTime lastModified;
    @Getter
    private final long size;
    private final String[] elements;

    private MinioItemDescription(MinioPath minioPath, Item result) {
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
