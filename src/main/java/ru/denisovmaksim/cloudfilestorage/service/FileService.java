package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.exception.FileStorageException;
import ru.denisovmaksim.cloudfilestorage.exception.StorageObjectNotFoundException;
import ru.denisovmaksim.cloudfilestorage.mapper.StorageObjectDTOMapper;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;
import ru.denisovmaksim.cloudfilestorage.storage.FileObject;
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
        fileStorage.createPath(securityService.getAuthUserId(), newFolderName);
    }

    public List<StorageObjectDTO> getContentOfDirectory(@ValidPath String path) {
        return fileStorage.getPathContent(securityService.getAuthUserId(), path)
                .orElseThrow(() -> new StorageObjectNotFoundException("Not found"))
                .stream()
                .map(StorageObjectDTOMapper::toDTO)
                .sorted(Comparator.comparing(StorageObjectDTO::getType).thenComparing(StorageObjectDTO::getName))
                .collect(Collectors.toList());
    }

    public void uploadFile(@ValidPath String path, MultipartFile file) {
        fileStorage.saveObject(securityService.getAuthUserId(), path, file);
    }

    public void renameFolder(@ValidPath String path, @ValidName String newFolderName) {
        fileStorage.renameFolder(securityService.getAuthUserId(), path, newFolderName);
    }

    public void deleteFolder(String path) {
        fileStorage.deleteObjects(securityService.getAuthUserId(), path);
    }

    public void deleteFile(@ValidPath String parentPath, @ValidName String fileName) {
        fileStorage.deleteObjects(securityService.getAuthUserId(), parentPath + fileName);
    }

    public NamedStreamDTO getFileAsStream(String path) {
        FileObject fileObject = fileStorage.getFileObject(securityService.getAuthUserId(), path);
        String encodedFileName = URLEncoder.encode(fileObject.path(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        //TODO Get name from path
        return new NamedStreamDTO(encodedFileName, fileObject.stream());
    }

    public NamedStreamDTO getZipFolderAsStream(@ValidPath String path) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        List<FileObject> fileObjects = fileStorage.getFileObjects(securityService.getAuthUserId(), path);
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
        //TODO Get name from path
        String encodedFileName = URLEncoder.encode("arch.zip", StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new NamedStreamDTO(encodedFileName, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    }

    public void uploadFolder(@ValidPath String path, List<MultipartFile> files) {
        files.forEach(file -> fileStorage.saveObject(securityService.getAuthUserId(), path, file));
    }
}
