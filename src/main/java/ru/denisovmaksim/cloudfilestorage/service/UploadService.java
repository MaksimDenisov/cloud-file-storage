package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.model.DirPath;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;

import java.util.List;

@Service
@Validated
@AllArgsConstructor
public class UploadService {
    private final StorageMetadataAccessor metadataAccessor;

    private final StorageDataAccessor dataAccessor;

    private final SecurityService securityService;


    public void uploadFile(DirPath parentDir, String filename,  MultipartFile multipartFile) {
        throwIfObjectExist(parentDir.resolveAsFile(filename).value());
        dataAccessor.saveObject(securityService.getAuthUserId(), parentDir.value(), multipartFile);
    }

    public void uploadFolder(DirPath dirPath, List<MultipartFile> files) {
        String filename = files.get(0).getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException();
        }
        String[] folders = filename.split("/");
        throwIfObjectExist(folders[0]);
        files.forEach(file -> dataAccessor.saveObject(securityService.getAuthUserId(), dirPath.value(), file));
    }

    private void throwIfObjectExist(String path) {
        if (metadataAccessor.exist(securityService.getAuthUserId(), path)) {
            throw new ObjectAlreadyExistException(String.format("Path %s already exist", path));
        }
    }
}
