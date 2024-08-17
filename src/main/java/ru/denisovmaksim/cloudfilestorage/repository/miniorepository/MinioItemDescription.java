package ru.denisovmaksim.cloudfilestorage.repository.miniorepository;

import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.Getter;
import ru.denisovmaksim.cloudfilestorage.exceptions.FileStorageException;
import ru.denisovmaksim.cloudfilestorage.model.StorageObjectType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;

final class MinioItemDescription {
    public static MinioItemDescription create(MinioPath minioPath, Result<Item> result) {
        try {
            Item item = result.get();
            return new MinioItemDescription(minioPath, item);
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
        size  = result.size();
        elements = minioName.replace(minioPath.getFullMinioPath(), "").split("/");
    }

    /**
     * Возвращает путь в папке, как его видит пользователь.
     *
     * @return Строка
     */
    public String getStoragePath() {
        return minioName.replace(minioPath.getUserFolder(), "");
    }

    /**
     * Возвращает имя объекта находящегося непосредственно в папке.*
     *
     * @return Строка
     */
    public String getDirectElementName() {
        return elements[0];
    }

    /**
     * Имя дочернего элемента.
     *
     * @return Null если отсутствует
     */
    @Nullable
    public String getChildElementName() {
        return (elements.length < 2) ? null : elements[1];
    }

    public boolean isRootFolder() {
        return elements[0].isEmpty();
    }

    public StorageObjectType getType() {
        return (minioName.endsWith("/")) ? StorageObjectType.FOLDER : StorageObjectType.UNKNOWN_FILE;
    }
}
