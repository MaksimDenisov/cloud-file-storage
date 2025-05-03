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
import ru.denisovmaksim.cloudfilestorage.validation.ValidName;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;

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


    public void createFolder(@ValidPath String path, @ValidName String folderName) {
        String newFolderName = path + folderName + "/";
        throwIfObjectExist(newFolderName);
        fileStorage.createPath(securityService.getAuthUserId(), newFolderName);
    }

    public List<StorageObjectDTO> getContentOfDirectory(@ValidPath String path) {
        return fileStorage.listObjectInfo(securityService.getAuthUserId(), path)
                .orElseThrow(() -> new NotFoundException(path))
                .stream()
                .map(StorageObjectDTOMapper::toDTO)
                .sorted(Comparator.comparing(StorageObjectDTO::getType).thenComparing(StorageObjectDTO::getName))
                .collect(Collectors.toList());
    }

    public void uploadFile(@ValidPath String path, MultipartFile file) {
        throwIfObjectExist(path);
        fileStorage.saveObject(securityService.getAuthUserId(), path, file);
    }


    public void renameFile(@ValidName String path, @ValidName String newFileName) {
        int lastSlashIndex = path.lastIndexOf('/');
        String parentPath = (lastSlashIndex == -1) ? "" : path.substring(0, lastSlashIndex);
        String newPath = parentPath + newFileName;
        throwIfObjectExist(newPath);
        fileStorage.copyOneObject(securityService.getAuthUserId(), path, newPath);
        fileStorage.deleteObjects(securityService.getAuthUserId(), path);
    }

    public void renameFolder(@ValidPath String path, @ValidName String newFolderName) {
        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlashIndex = path.lastIndexOf('/');
        String parentPath = (lastSlashIndex == -1) ? "" : path.substring(0, lastSlashIndex) + "/";
        String newPath = parentPath + newFolderName + "/";
        throwIfObjectExist(newPath);
        fileStorage.copyObjects(securityService.getAuthUserId(), path + "/", newPath);
        fileStorage.deleteObjects(securityService.getAuthUserId(), path + "/");
    }

    public void deleteFolder(String path) {
        fileStorage.deleteObjects(securityService.getAuthUserId(), path);
    }

    public void deleteFile(@ValidPath String parentPath, @ValidName String fileName) {
        fileStorage.deleteObjects(securityService.getAuthUserId(), parentPath + fileName);
    }

    public NamedStreamDTO getFileAsStream(String path) {
        FileObject fileObject = fileStorage.getObject(securityService.getAuthUserId(), path);
        String[] pathElements = path.split("/");
        String filename = pathElements[pathElements.length - 1];
        String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new NamedStreamDTO(encodedFileName, fileObject.stream());
    }

    public NamedStreamDTO getZipFolderAsStream(@ValidPath String path) {
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

    public void uploadFolder(@ValidPath String path, List<MultipartFile> files) {
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
