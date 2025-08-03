package ru.denisovmaksim.cloudfilestorage.service;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.dto.RequestUploadFileDTO;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.service.archive.ZipArchiver;
import ru.denisovmaksim.cloudfilestorage.storage.FileObject;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;
import ru.denisovmaksim.cloudfilestorage.validation.PathType;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Validated
@AllArgsConstructor
public class TransferService {

    private final MinioFileStorage fileStorage;

    private final SecurityService securityService;

    private final ZipArchiver zipArchiver;

    public void uploadFile(@ValidPath(PathType.DIR) String parentDirectory, @Valid RequestUploadFileDTO file) {
        throwIfObjectExist(parentDirectory + file.getFilename());
        MultipartFile multipartFile = file.getMultipartFile();
        fileStorage.saveObject(securityService.getAuthUserId(), parentDirectory, multipartFile);
    }


    public NamedStreamDTO getFileAsStream(@ValidPath(PathType.FILEPATH) String filepath) {
        FileObject fileObject = fileStorage.getObject(securityService.getAuthUserId(), filepath);
        String baseName = PathUtil.getBaseName(filepath);
        String encodedFileName = URLEncoder.encode(baseName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new NamedStreamDTO(encodedFileName, fileObject.size(), fileObject.stream());
    }

    public NamedStreamDTO getZipFolderAsStream(@ValidPath(PathType.DIR) String path) {
        String filename = PathUtil.getBaseName(path) + ".zip";
        String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        List<FileObject> fileObjects = fileStorage.getObjects(securityService.getAuthUserId(), path);
        ByteArrayOutputStream byteArrayOutputStream = zipArchiver.getByteArrayOutputStream(fileObjects, path);
        return new NamedStreamDTO(encodedFileName, byteArrayOutputStream.size(),
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    }

    public void uploadFolder(@ValidPath(PathType.DIR) String path, List<MultipartFile> files) {
        String filename = files.get(0).getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException();
        }
        String[] folders = filename.split("/");
        throwIfObjectExist(folders[0]);
        files.forEach(file -> fileStorage.saveObject(securityService.getAuthUserId(), path, file));
    }

    private void throwIfObjectExist(String path) {
        if (fileStorage.isExist(securityService.getAuthUserId(), path)) {
            throw new ObjectAlreadyExistException(String.format("Path %s already exist", path));
        }
    }
}
