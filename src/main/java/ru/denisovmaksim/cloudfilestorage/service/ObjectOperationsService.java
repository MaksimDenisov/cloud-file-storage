package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.exception.RootFolderException;
import ru.denisovmaksim.cloudfilestorage.model.DirPath;
import ru.denisovmaksim.cloudfilestorage.model.FilePath;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;


@Service
@Validated
@AllArgsConstructor
public class ObjectOperationsService {

    private final StorageMetadataAccessor storageMetadataAccessor;

    private final StorageDataAccessor storageDataAccessor;

    private final SecurityService securityService;

    public void createFolder(DirPath path) {
        Long authUserId = securityService.getAuthUserId();
        String newDirPath = path.value();
        throwIfObjectExist(newDirPath);
        storageMetadataAccessor.createPath(authUserId, newDirPath);
    }

    public void renameFile(FilePath filePath, String newFileName) {
        Long authUserId = securityService.getAuthUserId();
        FilePath newFilePath = filePath.renameTo(newFileName);
        throwIfObjectExist(newFilePath.value());
        storageDataAccessor.copyOneObject(authUserId, filePath.value(), newFilePath.value());
        storageDataAccessor.deleteOneObject(authUserId, filePath.value());
    }

    public void renameFolder(DirPath dirPath, String newFolderName) {
        throwIfRootModification(dirPath);
        FilePath newFilePath = dirPath.parent().resolveAsFile(newFolderName);
        DirPath newDirPath = dirPath.renameTo(newFolderName);
        throwIfObjectExist(newFilePath.value()); // folder with same name can't exist
        throwIfObjectExist(newDirPath.value());

        Long authUserId = securityService.getAuthUserId();
        if (storageDataAccessor.copyObjects(authUserId, dirPath.value(), newDirPath.value()) == 0) {
            storageMetadataAccessor.createPath(authUserId, newDirPath.value());
        }
        storageDataAccessor.deleteObjects(authUserId, dirPath.value());
    }

    public void deleteFolder(DirPath dirPath) {
        throwIfRootModification(dirPath);
        throwIfObjectNotExist(dirPath.value());
        Long authUserId = securityService.getAuthUserId();
        storageDataAccessor.deleteObjects(authUserId, dirPath.value());
        if (!storageMetadataAccessor.exist(authUserId, dirPath.parent().value())) {
            storageMetadataAccessor.createPath(authUserId, dirPath.parent().value());
        }
    }

    public void deleteFile(FilePath filePath) {
        Long authUserId = securityService.getAuthUserId();
        throwIfObjectNotExist(filePath.value());
        storageDataAccessor.deleteOneObject(authUserId, filePath.value());
        if (!storageMetadataAccessor.exist(authUserId, filePath.parent().value())) {
            storageMetadataAccessor.createPath(authUserId, filePath.parent().value());
        }
    }

    private void throwIfObjectExist(String path) {
        Long authUserId = securityService.getAuthUserId();
        if (storageMetadataAccessor.exist(authUserId, path)) {
            throw new ObjectAlreadyExistException(String.format("Path %s already exist", path));
        }
    }

    private void throwIfObjectNotExist(String path) {
        Long authUserId = securityService.getAuthUserId();
        if (!storageMetadataAccessor.exist(authUserId, path)) {
            throw new NotFoundException(String.format("Path %s doesn't exist", path));
        }
    }

    private void throwIfRootModification(DirPath dirPath) {
        if (dirPath.isRoot()) {
            throw new RootFolderException("The root folder cannot be modified");
        }
    }
}
