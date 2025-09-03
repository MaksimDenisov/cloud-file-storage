package ru.denisovmaksim.cloudfilestorage.service;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.dto.RequestUploadFileDTO;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.service.processing.ZipArchiver;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObject;
import ru.denisovmaksim.cloudfilestorage.storage.MinioDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.MinioMetadataAccessor;
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

    private final MinioMetadataAccessor metadataAccessor;

    private final MinioDataAccessor dataAccessor;

    private final SecurityService securityService;

    private final ZipArchiver zipArchiver;

    public void uploadFile(@ValidPath(PathType.DIR) String parentDirectory, @Valid RequestUploadFileDTO file) {
        throwIfObjectExist(parentDirectory + file.getFilename());
        MultipartFile multipartFile = file.getMultipartFile();
        dataAccessor.saveObject(securityService.getAuthUserId(), parentDirectory, multipartFile);
    }


    public NamedStreamDTO getFileAsStream(@ValidPath(PathType.FILEPATH) String filepath) {
        StorageObject storageObject = dataAccessor.getObject(securityService.getAuthUserId(), filepath);
        String baseName = PathUtil.getBaseName(filepath);
        String encodedFileName = URLEncoder.encode(baseName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        long size = metadataAccessor.getSize(securityService.getAuthUserId(), filepath);
        return new NamedStreamDTO(encodedFileName, size, storageObject.stream());
    }

    public NamedStreamDTO getZipFolderAsStream(@ValidPath(PathType.DIR) String path) {
        String filename = PathUtil.getBaseName(path) + ".zip";
        String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        List<StorageObject> fileObjects = dataAccessor.getObjects(securityService.getAuthUserId(), path);
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
        files.forEach(file -> dataAccessor.saveObject(securityService.getAuthUserId(), path, file));
    }

    private void throwIfObjectExist(String path) {
        if (metadataAccessor.isExist(securityService.getAuthUserId(), path)) {
            throw new ObjectAlreadyExistException(String.format("Path %s already exist", path));
        }
    }
}
