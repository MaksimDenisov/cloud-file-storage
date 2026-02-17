package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.dto.response.NamedStreamDTOResponse;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.service.processing.ZipArchiver;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObject;
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
public class DownloadService {

    private final StorageMetadataAccessor metadataAccessor;

    private final StorageDataAccessor dataAccessor;

    private final SecurityService securityService;

    private final ZipArchiver zipArchiver;

    public NamedStreamDTOResponse getFileAsStream(@ValidPath(PathType.FILEPATH) String filepath) {
        long size = metadataAccessor.getOne(securityService.getAuthUserId(), filepath)
                .orElseThrow(() -> new NotFoundException(filepath))
                .size();
        StorageObject storageObject = dataAccessor.getObject(securityService.getAuthUserId(), filepath);
        String baseName = PathUtil.getBaseName(filepath);
        String encodedFileName = URLEncoder.encode(baseName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new NamedStreamDTOResponse(encodedFileName, size, storageObject.stream());
    }

    public NamedStreamDTOResponse getZipFolderAsStream(@ValidPath(PathType.DIR) String path) {
        String filename = PathUtil.getBaseName(path) + ".zip";
        String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        List<StorageObject> fileObjects = dataAccessor.getObjects(securityService.getAuthUserId(), path);
        ByteArrayOutputStream byteArrayOutputStream = zipArchiver.getByteArrayOutputStream(fileObjects, path);
        return new NamedStreamDTOResponse(encodedFileName, byteArrayOutputStream.size(),
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    }
}
