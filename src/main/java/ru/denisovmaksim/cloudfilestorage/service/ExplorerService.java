package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
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

    public void createDirectory(@ValidPath(PathType.DIR) String parentDirectory,
                                @ValidPath(PathType.FILENAME) String directoryName) {
        String newFolderPath = parentDirectory + directoryName;
        if (!newFolderPath.endsWith("/")) {
            newFolderPath = newFolderPath + "/";
        }
        throwIfObjectExist(newFolderPath);
        fileStorage.createPath(securityService.getAuthUserId(), newFolderPath);
    }

    public List<StorageObjectDTO> getContentOfDirectory(@ValidPath(PathType.DIR) String directory) {
        Long userId = securityService.getAuthUserId();
        List<StorageObjectInfo> infos = fileStorage.listObjectInfo(userId, directory)
                .orElseThrow(() -> new NotFoundException(directory));
        //TODO add calculate size option probably extract another method
        for (StorageObjectInfo info : infos) {
            if (info.isDir()) {
                info.setSize(fileStorage.getDirectChildCount(userId, info.getPath()));
            }
        }
        return infos.stream()
                .map(StorageObjectDTOMapper::toDTO)
                .sorted(Comparator.comparing(StorageObjectDTO::getType).thenComparing(StorageObjectDTO::getName))
                .collect(Collectors.toList());
    }

    public void renameFile(@ValidPath(PathType.DIR) String parentPath,
                           @ValidPath(PathType.FILENAME) String currentFileName,
                           @ValidPath(PathType.FILENAME) String newFileName) {
        String dstPath = parentPath + newFileName;
        String srcPath = parentPath + currentFileName;
        throwIfObjectExist(dstPath);
        fileStorage.copyOneObject(securityService.getAuthUserId(), srcPath, dstPath);
        fileStorage.deleteObjects(securityService.getAuthUserId(), srcPath);
    }

    public void renameFolder(@ValidPath(PathType.DIR) String currentPath,
                             @ValidPath(PathType.FILENAME) String newFolderName) {
        String newPath = PathUtil.getParentDirName(currentPath) + newFolderName;
        if (!newPath.endsWith("/")) {
            newPath = newPath + "/";
        }
        throwIfObjectExist(newPath);
        fileStorage.copyObjects(securityService.getAuthUserId(), currentPath, newPath);
        fileStorage.deleteObjects(securityService.getAuthUserId(), currentPath);
    }

    public void deleteFolder(@ValidPath(PathType.DIR)String path) {
        String parentPath = PathUtil.getParentDirName(path);
        fileStorage.deleteObjects(securityService.getAuthUserId(), path);
        if (!fileStorage.isExist(securityService.getAuthUserId(), parentPath)) {
            fileStorage.createPath(securityService.getAuthUserId(), parentPath);
        }
    }

    public void deleteFile(@ValidPath(PathType.DIR) String parentPath, @ValidPath(PathType.FILENAME) String fileName) {
        fileStorage.deleteObjects(securityService.getAuthUserId(), parentPath + fileName);
        if (!fileStorage.isExist(securityService.getAuthUserId(), parentPath)) {
            fileStorage.createPath(securityService.getAuthUserId(), parentPath);
        }
    }

    private void throwIfObjectExist(String path) {
        if (fileStorage.isExist(securityService.getAuthUserId(), path)) {
            throw new ObjectAlreadyExistException(String.format("Path %s already exist", path));
        }
    }
}
