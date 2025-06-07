package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.exception.FileStorageException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.storage.FileObject;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;
import ru.denisovmaksim.cloudfilestorage.validation.PathType;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Validated
@AllArgsConstructor
public class TransferService {

    private final MinioFileStorage fileStorage;

    private final SecurityService securityService;


    public void uploadFile(@ValidPath(PathType.DIR) String parentDirectory, MultipartFile file) {
        throwIfObjectExist(parentDirectory + file.getOriginalFilename());
        fileStorage.saveObject(securityService.getAuthUserId(), parentDirectory, file);
    }


    public NamedStreamDTO getFileAsStream(@ValidPath(PathType.DIR) String path,
                                          @ValidPath(PathType.FILENAME) String filename) {
        FileObject fileObject = fileStorage.getObject(securityService.getAuthUserId(), path + filename);
        String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new NamedStreamDTO(encodedFileName, fileObject.stream());
    }

    public NamedStreamDTO getZipFolderAsStream(@ValidPath(PathType.DIR) String path) {
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

    public void uploadFolder(@ValidPath(PathType.DIR) String path, List<MultipartFile> files) {
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
