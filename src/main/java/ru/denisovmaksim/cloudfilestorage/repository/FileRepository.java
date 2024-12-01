package ru.denisovmaksim.cloudfilestorage.repository;

import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface FileRepository {
    void createFolder(Long userId, String pathOfCurrentUser, String folderName);

    List<StorageObject> getStorageObjects(Long authUserId, String path);

    void renameFolder(Long userId, String path, String newFolderName);

    void deleteObjects(Long authUserId, String folderName);

    void saveObject(Long authUserId, String path, MultipartFile file);

    InputStream getObjectAsStream(Long userId, String path);

    Map<String, InputStream> getObjectsAsStreams(Long authUserId, String path);

}
