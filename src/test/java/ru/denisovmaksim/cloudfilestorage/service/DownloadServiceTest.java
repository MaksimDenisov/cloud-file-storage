package ru.denisovmaksim.cloudfilestorage.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denisovmaksim.cloudfilestorage.dto.response.NamedStreamDTOResponse;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public abstract class DownloadServiceTest {
    @Mock
    private StorageMetadataAccessor minioMetadataAccessor;

    @Mock
    private StorageDataAccessor storageDataAccessor;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private DownloadService downloadService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUser() {
        when(securityService.getAuthUserId()).thenReturn(USER_ID);
    }

    @Test
    @DisplayName("Get file should return stream.")
    void getFileShouldReturnStreamDTO() {
        InputStream stream = new ByteArrayInputStream("test".getBytes());

        when(storageDataAccessor.getObject(USER_ID, "dir/file.txt"))
                .thenReturn(new StorageObject("dir/file.txt",  stream));

        NamedStreamDTOResponse result = downloadService.getFileAsStream("dir/file.txt");
        assertEquals("file.txt", java.net.URLDecoder.decode(result.getName(), java.nio.charset.StandardCharsets.UTF_8));
    }
}
