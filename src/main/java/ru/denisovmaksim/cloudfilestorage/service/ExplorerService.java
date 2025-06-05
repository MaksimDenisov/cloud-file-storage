package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.exception.FileStorageException;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.mapper.StorageObjectDTOMapper;
import ru.denisovmaksim.cloudfilestorage.storage.FileObject;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;
import ru.denisovmaksim.cloudfilestorage.util.FilePathUtil;
import ru.denisovmaksim.cloudfilestorage.validation.ValidFileName;
import ru.denisovmaksim.cloudfilestorage.validation.ValidDirPath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
@Validated
@AllArgsConstructor
public class ExplorerService {

    private final MinioFileStorage fileStorage;

    private final SecurityService securityService;

    public void createDirectory(@ValidDirPath String parentDirectory, @ValidFileName String directoryName) {
        String newFolderPath = parentDirectory + directoryName;
        if (!newFolderPath.endsWith("/")) {
            newFolderPath = newFolderPath + "/";
        }
        throwIfObjectExist(newFolderPath);
        fileStorage.createPath(securityService.getAuthUserId(), newFolderPath);
    }

    public List<StorageObjectDTO> getContentOfDirectory(@ValidDirPath String directory) {
        Long userId = securityService.getAuthUserId();
        List<StorageObjectInfo> infos = fileStorage.listObjectInfo(userId, directory)
                .orElseThrow(() -> new NotFoundException(directory));
        //TODO add calculate size option probably extract another method
        for (StorageObjectInfo info : infos) {
            if (info.isFolder()) {
                info.setSize(fileStorage.getDirectChildCount(userId, info.getPath()));
            }
        }
        return infos.stream()
                .map(StorageObjectDTOMapper::toDTO)
                .sorted(Comparator.comparing(StorageObjectDTO::getType).thenComparing(StorageObjectDTO::getName))
                .collect(Collectors.toList());
    }

    public void renameFile(@ValidDirPath String parentPath,
                           @ValidFileName String currentFileName,
                           @ValidFileName String newFileName) {
        String dstPath = parentPath + newFileName;
        String srcPath = parentPath + currentFileName;
        throwIfObjectExist(dstPath);
        fileStorage.copyOneObject(securityService.getAuthUserId(), srcPath, dstPath);
        fileStorage.deleteObjects(securityService.getAuthUserId(), srcPath);
    }

    public void renameFolder(@ValidDirPath String currentPath, @ValidFileName String newFolderName) {
        String newPath = FilePathUtil.getParentPath(currentPath) + newFolderName;
        if (!newPath.endsWith("/")) {
            newPath = newPath + "/";
        }
        throwIfObjectExist(newPath);
        fileStorage.copyObjects(securityService.getAuthUserId(), currentPath, newPath);
        fileStorage.deleteObjects(securityService.getAuthUserId(), currentPath);
    }

    public void deleteFolder(@ValidDirPath String path) {
        String parentPath = FilePathUtil.getParentPath(path);
        fileStorage.deleteObjects(securityService.getAuthUserId(), path);
        if (!fileStorage.isExist(securityService.getAuthUserId(), parentPath)) {
            fileStorage.createPath(securityService.getAuthUserId(), parentPath);
        }
    }

    public void deleteFile(@ValidDirPath String parentPath, @ValidFileName String fileName) {
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
