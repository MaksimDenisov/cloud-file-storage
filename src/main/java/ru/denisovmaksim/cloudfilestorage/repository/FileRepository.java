package ru.denisovmaksim.cloudfilestorage.repository;

import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;

import java.util.List;

public interface FileRepository {
    void createFolder(Long userId, String pathOfCurrentUser, String folderName);

    List<StorageObject> getStorageObjects(Long authUserId, String path);

    void deleteFolder(Long authUserId, String folderName);

    void uploadFile(Long authUserId, String path, MultipartFile file);
}
