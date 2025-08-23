package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.exception.RootFolderModificationException;
import ru.denisovmaksim.cloudfilestorage.mapper.StorageObjectDTOMapper;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;
import ru.denisovmaksim.cloudfilestorage.validation.PathType;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Validated
@AllArgsConstructor
public class ExplorerService {

    private final MinioFileStorage fileStorage;

    private final SecurityService securityService;

    public void createFolder(@ValidPath(PathType.DIR) String path) {
        Long authUserId = securityService.getAuthUserId();
        String newDirPath = PathUtil.ensureDirectoryPath(path);
        throwIfObjectExist(newDirPath);
        fileStorage.createPath(authUserId, newDirPath);
    }

    public List<StorageObjectDTO> getFolder(@ValidPath(PathType.DIR) String directory) {
        Long authUserId = securityService.getAuthUserId();
        List<StorageObjectInfo> infos = fileStorage.listObjectInfo(authUserId, directory)
                .orElseThrow(() -> new NotFoundException(directory));
        for (StorageObjectInfo info : infos) {
            if (info.isDir()) {
                info.setSize(fileStorage.getDirectChildCount(authUserId, info.getPath()));
            }
        }
        return infos.stream()
                .map(StorageObjectDTOMapper::toDTO)
                .sorted(Comparator.comparing(StorageObjectDTO::getType).thenComparing(StorageObjectDTO::getName))
                .collect(Collectors.toList());
    }

    public void renameFile(@ValidPath(PathType.FILEPATH) String filepath,
                           @ValidPath(PathType.FILENAME) String newFileName) {
        Long authUserId = securityService.getAuthUserId();
        String parentDirectory = PathUtil.getParentDirName(filepath);
        String dstPath = parentDirectory + newFileName;
        throwIfObjectExist(dstPath);
        fileStorage.copyOneObject(authUserId, filepath, dstPath);
        fileStorage.deleteObjects(authUserId, filepath);
    }

    public void renameFolder(@ValidPath(PathType.DIR) String directory,
                             @ValidPath(PathType.FILENAME) String newFolderName) {
        throwIfRootModification(directory);
        String newPath = PathUtil.getParentDirName(directory) + newFolderName;
        newPath = PathUtil.ensureDirectoryPath(newPath);
        throwIfObjectExist(newPath);

        Long authUserId = securityService.getAuthUserId();
        if (fileStorage.copyObjects(authUserId, directory, newPath) == 0) {
            fileStorage.createPath(authUserId, newPath);
        }
        fileStorage.deleteObjects(authUserId, directory);
    }

    public void deleteFolder(@ValidPath(PathType.DIR) String directory) {
        throwIfRootModification(directory);
        Long authUserId = securityService.getAuthUserId();
        String parentPath = PathUtil.getParentDirName(directory);
        fileStorage.deleteObjects(authUserId, directory);
        if (!fileStorage.isExist(authUserId, parentPath)) {
            fileStorage.createPath(authUserId, parentPath);
        }
    }

    public void deleteFile(@ValidPath(PathType.FILEPATH) String filePath) {
        Long authUserId = securityService.getAuthUserId();
        String parentDirectory = PathUtil.getParentDirName(filePath);
        fileStorage.deleteObjects(authUserId, filePath);
        if (!fileStorage.isExist(authUserId, parentDirectory)) {
            fileStorage.createPath(authUserId, parentDirectory);
        }
    }

    private void throwIfObjectExist(String path) {
        Long authUserId = securityService.getAuthUserId();
        if (fileStorage.isExist(authUserId, path)) {
            throw new ObjectAlreadyExistException(String.format("Path %s already exist", path));
        }
    }

    private void throwIfRootModification(String path) {
        if (PathUtil.isRoot(path)) {
            throw new RootFolderModificationException("The root folder cannot be modified");
        }
    }
}
