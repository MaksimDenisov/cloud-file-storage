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
public class FileService {

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
        return fileStorage.listObjectInfo(securityService.getAuthUserId(), directory)
                .orElseThrow(() -> new NotFoundException(directory))
                .stream()
                .map(StorageObjectDTOMapper::toDTO)
                .sorted(Comparator.comparing(StorageObjectDTO::getType).thenComparing(StorageObjectDTO::getName))
                .collect(Collectors.toList());
    }

    public void uploadFile(@ValidDirPath String parentDirectory, MultipartFile file) {
        throwIfObjectExist(parentDirectory + file.getOriginalFilename());
        fileStorage.saveObject(securityService.getAuthUserId(), parentDirectory, file);
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

    public NamedStreamDTO getFileAsStream(@ValidDirPath String path, @ValidFileName String filename) {
        FileObject fileObject = fileStorage.getObject(securityService.getAuthUserId(), path + filename);
        String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new NamedStreamDTO(encodedFileName, fileObject.stream());
    }

    public NamedStreamDTO getZipFolderAsStream(@ValidDirPath String path) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        List<FileObject> fileObjects = fileStorage.getObjects(securityService.getAuthUserId(), path);
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (FileObject object : fileObjects) {
                String objectPath = object.path().replaceFirst(path, "");
                ZipEntry zipEntry = new ZipEntry(objectPath);
                zipOutputStream.putNextEntry(zipEntry);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream objectInputStream = object.stream();
                while ((bytesRead = objectInputStream.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, bytesRead);
                }
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new FileStorageException(e);
        }
        String[] pathElements = path.split("/");
        String filename = pathElements[pathElements.length - 1] + ".zip";
        String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new NamedStreamDTO(encodedFileName, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    }

    public void uploadFolder(@ValidDirPath String path, List<MultipartFile> files) {
        String[] folders = files.get(0)
                .getOriginalFilename()
                .split("/");
        throwIfObjectExist(folders[0]);
        files.forEach(file -> fileStorage.saveObject(securityService.getAuthUserId(), path, file));
    }

    private void throwIfObjectExist(String path) {
        if (fileStorage.isExist(securityService.getAuthUserId(), path)) {
            throw new ObjectAlreadyExistException(String.format("Path %s already exist", path));
        }
    }
}
