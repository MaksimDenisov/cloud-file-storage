package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.exception.RootFolderModificationException;
import ru.denisovmaksim.cloudfilestorage.mapper.StorageObjectDTOMapper;
import ru.denisovmaksim.cloudfilestorage.storage.MinioDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.MinioMetadataAccessor;
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

    private final MinioMetadataAccessor minioMetadataAccessor;

    private final MinioDataAccessor minioDataAccessor;

    private final SecurityService securityService;

    public void createFolder(@ValidPath(PathType.DIR) String path) {
        Long authUserId = securityService.getAuthUserId();
        String newDirPath = PathUtil.ensureDirectoryPath(path);
        throwIfObjectExist(newDirPath);
        minioDataAccessor.createPath(authUserId, newDirPath);
    }

    public List<StorageObjectDTO> getFolder(@ValidPath(PathType.DIR) String directory) {
        Long authUserId = securityService.getAuthUserId();
        List<StorageObjectInfo> infos = minioMetadataAccessor.listObjectInfo(authUserId, directory)
                .orElseThrow(() -> new NotFoundException(directory));
        for (StorageObjectInfo info : infos) {
            if (info.isDir()) {
                info.setSize(minioMetadataAccessor.getDirectChildCount(authUserId, info.getPath()));
            }
        }
        return infos.stream()
                .map(StorageObjectDTOMapper::toDTO)
                .sorted(Comparator.comparing(StorageObjectDTO::getType).thenComparing(StorageObjectDTO::getName))
                .collect(Collectors.toList());
    }

    public Long getSize(@ValidPath(PathType.FILEPATH) String filepath) {
        Long authUserId = securityService.getAuthUserId();
        return minioMetadataAccessor.getSize(authUserId, filepath);
    }

    public void renameFile(@ValidPath(PathType.FILEPATH) String filepath,
                           @ValidPath(PathType.FILENAME) String newFileName) {
        Long authUserId = securityService.getAuthUserId();
        String parentDirectory = PathUtil.getParentDirName(filepath);
        String dstPath = parentDirectory + newFileName;
        throwIfObjectExist(dstPath);
        minioDataAccessor.copyOneObject(authUserId, filepath, dstPath);
        minioDataAccessor.deleteObjects(authUserId, filepath);
    }

    public void renameFolder(@ValidPath(PathType.DIR) String directory,
                             @ValidPath(PathType.FILENAME) String newFolderName) {
        throwIfRootModification(directory);
        String newPath = PathUtil.getParentDirName(directory) + newFolderName;
        newPath = PathUtil.ensureDirectoryPath(newPath);
        throwIfObjectExist(newPath);

        Long authUserId = securityService.getAuthUserId();
        if (minioDataAccessor.copyObjects(authUserId, directory, newPath) == 0) {
            minioDataAccessor.createPath(authUserId, newPath);
        }
        minioDataAccessor.deleteObjects(authUserId, directory);
    }

    public void deleteFolder(@ValidPath(PathType.DIR) String directory) {
        throwIfRootModification(directory);
        Long authUserId = securityService.getAuthUserId();
        String parentPath = PathUtil.getParentDirName(directory);
        minioDataAccessor.deleteObjects(authUserId, directory);
        if (!minioMetadataAccessor.isExist(authUserId, parentPath)) {
            minioDataAccessor.createPath(authUserId, parentPath);
        }
    }

    public void deleteFile(@ValidPath(PathType.FILEPATH) String filePath) {
        Long authUserId = securityService.getAuthUserId();
        String parentDirectory = PathUtil.getParentDirName(filePath);
        minioDataAccessor.deleteObjects(authUserId, filePath);
        if (!minioMetadataAccessor.isExist(authUserId, parentDirectory)) {
            minioDataAccessor.createPath(authUserId, parentDirectory);
        }
    }

    private void throwIfObjectExist(String path) {
        Long authUserId = securityService.getAuthUserId();
        if (minioMetadataAccessor.isExist(authUserId, path)) {
            throw new ObjectAlreadyExistException(String.format("Path %s already exist", path));
        }
    }

    private void throwIfRootModification(String path) {
        if (PathUtil.isRoot(path)) {
            throw new RootFolderModificationException("The root folder cannot be modified");
        }
    }
}
