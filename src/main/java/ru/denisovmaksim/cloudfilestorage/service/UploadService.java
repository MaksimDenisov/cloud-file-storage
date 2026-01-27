package ru.denisovmaksim.cloudfilestorage.service;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.dto.request.UploadFileDTORequest;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.storage.MinioDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.MinioMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.validation.PathType;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;

import java.util.List;

@Service
@Validated
@AllArgsConstructor
public class UploadService {
    private final MinioMetadataAccessor metadataAccessor;

    private final MinioDataAccessor dataAccessor;

    private final SecurityService securityService;


    public void uploadFile(@ValidPath(PathType.DIR) String parentDirectory, @Valid UploadFileDTORequest file) {
        throwIfObjectExist(parentDirectory + file.getFilename());
        MultipartFile multipartFile = file.getMultipartFile();
        dataAccessor.saveObject(securityService.getAuthUserId(), parentDirectory, multipartFile);
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
        if (metadataAccessor.isExistByPrefix(securityService.getAuthUserId(), path)) {
            throw new ObjectAlreadyExistException(String.format("Path %s already exist", path));
        }
    }
}
