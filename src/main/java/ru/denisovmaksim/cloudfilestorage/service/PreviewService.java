package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.exception.ImageProcessingException;
import ru.denisovmaksim.cloudfilestorage.service.processing.ImageResizer;
import ru.denisovmaksim.cloudfilestorage.storage.MinioDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObject;
import ru.denisovmaksim.cloudfilestorage.util.FileTypeResolver;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Validated
@AllArgsConstructor
public class PreviewService {

    private static final int IMAGE_WIDTH = 800;

    private final MinioDataAccessor dataAccessor;

    private final SecurityService securityService;

    private final ImageResizer imageResizer;

    public NamedStreamDTO getImage(String filepath) {
        StorageObject storageObject = dataAccessor.getObject(securityService.getAuthUserId(), filepath);
        String baseName = PathUtil.getBaseName(filepath);
        String encodedFileName = URLEncoder.encode(baseName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        InputStream resizedStream = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            resizedStream = imageResizer.shrinkIfWiderThan(storageObject.stream(),
                    FileTypeResolver.getExtension(baseName),
                    IMAGE_WIDTH, os);
        } catch (Exception e) {
            throw new ImageProcessingException(e);
        }
        return new NamedStreamDTO(encodedFileName, os.size(), resizedStream);
    }
}
