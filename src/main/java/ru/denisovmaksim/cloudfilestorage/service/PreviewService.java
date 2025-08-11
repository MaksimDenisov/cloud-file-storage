package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.storage.FileObject;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;
import ru.denisovmaksim.cloudfilestorage.validation.PathType;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Validated
@AllArgsConstructor
public class PreviewService {

    private static final int IMAGE_WIDTH = 800;

    private final MinioFileStorage fileStorage;

    private final SecurityService securityService;

    private final ImageResizer imageResizer;

    public NamedStreamDTO getMusic(@ValidPath(PathType.FILEPATH) String filepath) {
        FileObject fileObject = fileStorage.getObject(securityService.getAuthUserId(), filepath);
        String baseName = PathUtil.getBaseName(filepath);
        String encodedFileName = URLEncoder.encode(baseName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new NamedStreamDTO(encodedFileName, fileObject.size(), fileObject.stream());
    }

    public NamedStreamDTO getImage(String filepath) {
        FileObject fileObject = fileStorage.getObject(securityService.getAuthUserId(), filepath);
        String baseName = PathUtil.getBaseName(filepath);
        String encodedFileName = URLEncoder.encode(baseName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        InputStream resizedStream = null;
        try {
            resizedStream = imageResizer.resizeKeepAspectRatioAndStream(fileObject.stream(), IMAGE_WIDTH);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new NamedStreamDTO(encodedFileName, fileObject.size(), resizedStream);
    }
}
