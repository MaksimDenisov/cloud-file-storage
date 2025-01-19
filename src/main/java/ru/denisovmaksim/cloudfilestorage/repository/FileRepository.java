package ru.denisovmaksim.cloudfilestorage.repository;

import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;
import ru.denisovmaksim.cloudfilestorage.model.StorageObjectStream;
import ru.denisovmaksim.cloudfilestorage.model.StorageObjectsStreams;

import java.util.List;

public interface FileRepository {
    void createEmptyPath(Long userId, String pathOfCurrentUser, String folderName);

    List<StorageObject> getStorageObjects(Long authUserId, String path);

    void renameFolder(Long userId, String path, String newFolderName);

    void deleteObjects(Long authUserId, String folderName);

    void saveObject(Long authUserId, String path, MultipartFile file);

    StorageObjectStream getObjectAsStream(Long userId, String path);

    StorageObjectsStreams getObjectsAsStreams(Long authUserId, String path);

}
