package ru.denisovmaksim.cloudfilestorage.repository.miniorepository;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.exceptions.FileStorageException;
import ru.denisovmaksim.cloudfilestorage.exceptions.StorageObjectNotFoundException;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;
import ru.denisovmaksim.cloudfilestorage.repository.FileRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@Profile({"dev", "prod"})
public class MinioFileRepository implements FileRepository {
    // https://min.io/docs/minio/linux/developers/java/minio-java.html
    // Examples
    // https://github.com/minio/minio-java/tree/release/examples

    //https://min.io/docs/minio/linux/developers/java/API.html
    private final MinioClient minioClient;

    private final String bucket;

    public MinioFileRepository(MinioClient minioClient, @Value("${app.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    public void createFolder(Long userId, String path, String folderName) {
        MinioPath minioPath = new MinioPath(userId, path);
        try {
            String newFolderName = minioPath.getUserFolder() + minioPath.getPath() + folderName + "/";
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(newFolderName)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new FileStorageException(e);
        }
    }

    public List<StorageObject> getStorageObjects(Long userId, String path) {
        MinioPath minioPath = new MinioPath(userId, path);
        throwNotFoundExceptionIfObjectNotExist(minioPath);
        Iterable<Result<Item>> minioItems = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(minioPath.getFullMinioPath())
                        .recursive(true)
                        .build());
        return toStorageObjects(minioPath, minioItems);
    }

    @Override
    public void deleteFolder(Long userId, String path) {
        MinioPath minioPath = new MinioPath(userId, path);
        throwNotFoundExceptionIfObjectNotExist(minioPath);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket)
                            .object(minioPath.getFullMinioPath())
                            .build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new FileStorageException(e);
        }
    }

    private void throwNotFoundExceptionIfObjectNotExist(MinioPath minioPath) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket)
                    .object(minioPath.getFullMinioPath())
                    .build());
        } catch (ErrorResponseException e) {
            throw new StorageObjectNotFoundException(minioPath.getPath());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new FileStorageException(e);
        }
    }

    private List<StorageObject> toStorageObjects(MinioPath minioPath, Iterable<Result<Item>> resultItems) {
        List<StorageObject> storageObjects = new ArrayList<>();
        Map<String, Set<String>> subElementsMap = new ConcurrentHashMap<>();

        for (Result<Item> resultItem : resultItems) {
            MinioItemDescription itemDescription = MinioItemDescription.create(minioPath, resultItem);

            String storagePath = itemDescription.getStoragePath();
            String directElementName = itemDescription.getDirectElementName();
            if (itemDescription.isRootFolder()) {
                continue;
            }
            String childElementName = itemDescription.getChildElementName();
            if (childElementName == null) {
                storageObjects.add(new StorageObject(storagePath, directElementName,
                        itemDescription.getType(), itemDescription.getSize(), itemDescription.getLastModified()));
            } else {
                subElementsMap.putIfAbsent(directElementName, new HashSet<>());
                subElementsMap.get(directElementName).add(childElementName);
            }
        }

        for (StorageObject object : storageObjects) {
            Set<String> subFolders = subElementsMap.get(object.getName());
            if (subFolders != null) {
                object.setSize((long) subFolders.size());
            }
        }
        return storageObjects;
    }
}
