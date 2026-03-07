package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.exception.RootFolderModificationException;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;
import ru.denisovmaksim.cloudfilestorage.validation.PathType;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;


@Service
@Validated
@AllArgsConstructor
public class ObjectOperationsService {

    private final StorageMetadataAccessor minioMetadataAccessor;

    private final StorageDataAccessor storageDataAccessor;

    private final SecurityService securityService;

    public void createFolder(@ValidPath(PathType.DIR) String path) {
        Long authUserId = securityService.getAuthUserId();
        String newDirPath = PathUtil.ensureDirectoryPath(path);
        throwIfObjectExist(newDirPath);
        minioMetadataAccessor.createPath(authUserId, newDirPath);
    }

    public void renameFile(@ValidPath(PathType.FILEPATH) String filepath,
                           @ValidPath(PathType.NAME) String newFileName) {
        Long authUserId = securityService.getAuthUserId();
        String parentDirectory = PathUtil.getParentPath(filepath);
        String dstPath = parentDirectory + newFileName;
        throwIfObjectExist(dstPath);
        storageDataAccessor.copyOneObject(authUserId, filepath, dstPath);
        storageDataAccessor.deleteOneObject(authUserId, filepath);
    }

    public void renameFolder(@ValidPath(PathType.DIR) String directory,
                             @ValidPath(PathType.NAME) String newFolderName) {
        throwIfRootModification(directory);
        String newPath = PathUtil.getParentPath(directory) + newFolderName;
        throwIfObjectExist(newPath); // file
        newPath = PathUtil.ensureDirectoryPath(newPath);
        throwIfObjectExist(newPath);

        Long authUserId = securityService.getAuthUserId();
        if (storageDataAccessor.copyObjects(authUserId, directory, newPath) == 0) {
            minioMetadataAccessor.createPath(authUserId, newPath);
        }
        storageDataAccessor.deleteObjects(authUserId, PathUtil.ensureDirectoryPath(directory));
    }

    public void deleteFolder(@ValidPath(PathType.DIR) String directory) {
        directory = PathUtil.ensureDirectoryPath(directory);
        throwIfRootModification(directory);
        throwIfObjectNotExist(directory);
        Long authUserId = securityService.getAuthUserId();
        String parentPath = PathUtil.getParentPath(directory);
        storageDataAccessor.deleteObjects(authUserId, directory);
        if (!minioMetadataAccessor.exist(authUserId, parentPath)) {
            minioMetadataAccessor.createPath(authUserId, parentPath);
        }
    }

    public void deleteFile(@ValidPath(PathType.FILEPATH) String filePath) {
        Long authUserId = securityService.getAuthUserId();
        throwIfObjectNotExist(filePath);
        String parentDirectory = PathUtil.getParentPath(filePath);
        storageDataAccessor.deleteOneObject(authUserId, filePath);
        if (!minioMetadataAccessor.exist(authUserId, parentDirectory)) {
            minioMetadataAccessor.createPath(authUserId, parentDirectory);
        }
    }

    private void throwIfObjectExist(String path) {
        Long authUserId = securityService.getAuthUserId();
        if (minioMetadataAccessor.exist(authUserId, path)) {
            throw new ObjectAlreadyExistException(String.format("Path %s already exist", path));
        }
    }

    private void throwIfObjectNotExist(String path) {
        Long authUserId = securityService.getAuthUserId();
        if (!minioMetadataAccessor.exist(authUserId, path)) {
            throw new NotFoundException(String.format("Path %s doesn't exist", path));
        }
    }

    private void throwIfRootModification(String path) {
        if (PathUtil.isRoot(path)) {
            throw new RootFolderModificationException("The root folder cannot be modified");
        }
    }
}
