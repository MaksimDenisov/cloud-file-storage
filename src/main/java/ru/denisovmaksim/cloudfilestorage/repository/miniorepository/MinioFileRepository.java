package ru.denisovmaksim.cloudfilestorage.repository.miniorepository;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.exceptions.StorageObjectNotFoundException;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;
import ru.denisovmaksim.cloudfilestorage.repository.FileRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.denisovmaksim.cloudfilestorage.repository.miniorepository.MinioExceptionHandler.interceptMinioExceptions;

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

    public void createEmptyPath(Long userId, String path, String folderName) {
        log.info("Create folder path = {} name = {} for user with id = {}", path, folderName, userId);
        MinioPath minioPath = new MinioPath(userId, path);
        interceptMinioExceptions(() -> {
                    String newFolderName = minioPath.getPathByMinio() + folderName + "/";
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(newFolderName)
                                    .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                                    .build());
                }
        );
    }

    public List<StorageObject> getStorageObjects(Long userId, String path) {
        log.info("Get objects from path = {} for user with id = {}", path, userId);
        MinioPath minioPath = new MinioPath(userId, path);
        Iterable<Result<Item>> resultItems = getMinioItems(minioPath);
        List<StorageObject> objects = new ArrayList<>();
        for (Result<Item> resultItem : resultItems) {
            Item item = interceptMinioExceptions(resultItem::get);
            String minioName = item.objectName();
            if (!minioName.equals(minioPath.getPathByMinio())) {
                StorageObject object = toStorageObjects(minioPath, item);
                if (object.isFolder()) {
                    object.setSize(getChildCount(new MinioPath(userId, object.getPath())));
                }
                objects.add(object);
            }
        }
        return objects;
    }

    @Override
    public void renameFolder(Long userId, String path, String newFolderName) {
        log.info("Rename folder {} for user with id = {}", path, userId);
        MinioPath minioPath = new MinioPath(userId, path);
        interceptMinioExceptions(() -> {
            Iterable<Result<Item>> minioItems = getMinioItemsRecursive(new MinioPath(userId, path));
            for (Result<Item> result : minioItems) {
                String sourceName = result.get().objectName();
                String destName = sourceName.replace(minioPath.getPathByMinio(),
                        minioPath.getParentMinioPath() + newFolderName + "/");
                copyObject(sourceName, destName);
            }
        });
        deleteObjects(userId, path);
    }

    @Override
    public void deleteObjects(Long userId, String path) {
        log.info("Delete folder {} for user with id = {}", path, userId);
        MinioPath minioPath = new MinioPath(userId, path);
        if (minioPath.isRoot()) {
            return;
        }
        interceptMinioExceptions(() -> {
            Iterable<Result<Item>> minioItems = getMinioItemsRecursive(minioPath);
            for (Result<Item> resultItem : minioItems) {
                minioClient.removeObject(
                        RemoveObjectArgs.builder().bucket(bucket)
                                .object(resultItem.get().objectName())
                                .build());
            }
        });
    }

    @Override
    public void saveObject(Long userId, String path, MultipartFile file) {
        MinioPath minioPath = new MinioPath(userId, path);
        interceptMinioExceptions(() -> {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(minioPath.getPathByMinio() + file.getOriginalFilename())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        });
    }

    @Override
    public InputStream getObjectAsStream(Long userId, String path) {
        MinioPath minioPath = new MinioPath(userId, path);
        return interceptMinioExceptions(() -> minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(minioPath.getPathByMinio())
                .build()));
    }

    @Override
    public Map<String, InputStream> getObjectsAsStreams(Long userId, String path) {
        MinioPath minioPath = new MinioPath(userId, path);
        Map<String, InputStream> result = new HashMap<>();
        return interceptMinioExceptions(() -> {
            Iterable<Result<Item>> minioItems = getMinioItemsRecursive(minioPath);
            for (Result<Item> resultItem : minioItems) {
                String objectName = resultItem.get().objectName();
                String name = objectName.replace(minioPath.getUserFolder(), "");
                InputStream objectInputStream = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build());
                result.put(name, objectInputStream);
            }
            return result;
        });
    }

    private Iterable<Result<Item>> getMinioItems(MinioPath minioPath) {
        Iterable<Result<Item>> minioItems = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(minioPath.getPathByMinio())
                        .build());
        if (!minioItems.iterator().hasNext() && !minioPath.isRoot()) {
            throw new StorageObjectNotFoundException(String.format("%s not exist", minioPath.getPathByUser()));
        }
        return minioItems;
    }

    private StorageObject toStorageObjects(MinioPath minioPath, Item item) {
        String minioName = item.objectName();
        String path = minioName.replace(minioPath.getUserFolder(), "");
        StorageObject object = new StorageObject(path);
        if (!object.isFolder()) {
            object.setSize(item.size());
        }
        return object;
    }

    private long getChildCount(MinioPath minioPath) {
        Iterable<Result<Item>> minioItems = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(minioPath.getPathByMinio())
                        .build());
        long count = 0;
        for (Result<Item> minioItem : minioItems) {
            Item item = interceptMinioExceptions(minioItem::get);
            if (!item.objectName().equals(minioPath.getPathByMinio())) {
                count++;
            }
        }
        return count;
    }

    private Iterable<Result<Item>> getMinioItemsRecursive(MinioPath minioPath) {
        Iterable<Result<Item>> minioItems = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(minioPath.getPathByMinio())
                        .recursive(true)
                        .build());
        if (!minioItems.iterator().hasNext() && !minioPath.isRoot()) {
            throw new StorageObjectNotFoundException(String.format("%s not exist", minioPath.getPathByUser()));
        }
        return minioItems;
    }

    private void copyObject(String sourceName, String destName) {
        interceptMinioExceptions(() ->
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucket)
                                .object(destName)
                                .source(
                                        CopySource.builder()
                                                .bucket(bucket)
                                                .object(sourceName)
                                                .build())
                                .build()));
    }
}
