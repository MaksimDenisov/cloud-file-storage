package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.dto.response.NamedStreamDTOResponse;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.model.DirPath;
import ru.denisovmaksim.cloudfilestorage.model.FilePath;
import ru.denisovmaksim.cloudfilestorage.service.processing.ZipArchiver;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObject;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

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

    public NamedStreamDTOResponse getFileAsStream(FilePath filepath) {
        long size = metadataAccessor.getOne(securityService.getAuthUserId(), filepath.value())
                .orElseThrow(() -> new NotFoundException(filepath.value()))
                .size();
        StorageObject storageObject = dataAccessor.getObject(securityService.getAuthUserId(), filepath.value());
        String baseName = PathUtil.getBaseName(filepath.value());
        String encodedFileName = URLEncoder.encode(baseName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new NamedStreamDTOResponse(encodedFileName, size, storageObject.stream());
    }

    public NamedStreamDTOResponse getZipFolderAsStream(DirPath dirPath) {
        String filename = dirPath.name() + ".zip";
        String encodedFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
        List<StorageObject> fileObjects = dataAccessor.getObjects(securityService.getAuthUserId(), dirPath.value());
        ByteArrayOutputStream outputStream = zipArchiver.getByteArrayOutputStream(fileObjects, dirPath.value());
        return new NamedStreamDTOResponse(encodedFileName, outputStream.size(),
                new ByteArrayInputStream(outputStream.toByteArray()));
    }
}
