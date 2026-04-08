package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.dto.response.NamedStreamDTOResponse;
import ru.denisovmaksim.cloudfilestorage.exception.ImageProcessingException;
import ru.denisovmaksim.cloudfilestorage.model.FilePath;
import ru.denisovmaksim.cloudfilestorage.service.processing.ImageResizer;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObject;
import ru.denisovmaksim.cloudfilestorage.util.FileTypeResolver;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Validated
@AllArgsConstructor
public class PreviewService {

    private static final int IMAGE_WIDTH = 800;

    private final StorageDataAccessor dataAccessor;

    private final SecurityService securityService;

    private final ImageResizer imageResizer;

    public NamedStreamDTOResponse getImage(FilePath filepath) {
        StorageObject storageObject = dataAccessor.getObject(securityService.getAuthUserId(), filepath.value());
        String baseName = filepath.name();
        String encodedFileName = URLEncoder.encode(baseName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        InputStream resizedStream;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            resizedStream = imageResizer.shrinkIfWiderThan(storageObject.stream(),
                    FileTypeResolver.getExtension(baseName),
                    IMAGE_WIDTH, os);
        } catch (Exception e) {
            throw new ImageProcessingException(e);
        }
        return new NamedStreamDTOResponse(encodedFileName, os.size(), resizedStream);
    }
}
