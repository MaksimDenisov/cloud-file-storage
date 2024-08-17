package ru.denisovmaksim.cloudfilestorage.repository;

import ru.denisovmaksim.cloudfilestorage.model.StorageObject;

import java.util.List;

public interface FileRepository {
    void createFolder(Long userId, String pathOfCurrentUser, String folderName);

    List<StorageObject> getStorageObjects(Long authUserId, String path);
}
