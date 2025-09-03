package ru.denisovmaksim.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.dto.RequestUploadFileDTO;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.storage.MinioDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.MinioMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private MinioMetadataAccessor minioMetadataAccessor;

    @Mock
    private MinioDataAccessor minioDataAccessor;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private TransferService transferService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUser() {
        when(securityService.getAuthUserId()).thenReturn(USER_ID);
    }

    @Test
    @DisplayName("Upload file should save it to storage.")
    void uploadFileShouldSaveWhenNotExists() {
        MultipartFile file = mock(MultipartFile.class);
        when(minioMetadataAccessor.isExist(USER_ID, "dir/file.txt")).thenReturn(false);
        RequestUploadFileDTO dto = new RequestUploadFileDTO("file.txt", file);
        transferService.uploadFile("dir/", dto);

        verify(minioDataAccessor).saveObject(USER_ID, "dir/", file);
    }

    @Test
    @DisplayName("Upload file should save it to storage.")
    void uploadFileShouldThrowWhenExists() {
        MultipartFile file = mock(MultipartFile.class);
        RequestUploadFileDTO dto = new RequestUploadFileDTO("file.txt", file);

        when(minioMetadataAccessor.isExist(eq(USER_ID), any())).thenReturn(true);

        assertThrows(ObjectAlreadyExistException.class,
                () -> transferService.uploadFile("dir/", dto));
    }

    @Test
    @DisplayName("Get file should return stream.")
    void getFileShouldReturnStreamDTO() {
        InputStream stream = new ByteArrayInputStream("test".getBytes());

        when(minioDataAccessor.getObject(USER_ID, "dir/file.txt"))
                .thenReturn(new StorageObject("dir/file.txt",  stream));

        NamedStreamDTO result = transferService.getFileAsStream("dir/file.txt");
        assertEquals("file.txt", java.net.URLDecoder.decode(result.getName(), java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Upload folder should save each file.")
    void uploadFolderShouldSaveEachFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("folder/file.txt");
        List<MultipartFile> files = List.of(file);

        when(minioMetadataAccessor.isExist(USER_ID, "folder")).thenReturn(false);

        transferService.uploadFolder("", files);

        verify(minioDataAccessor).saveObject(USER_ID, "", file);
    }
}
