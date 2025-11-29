package ru.denisovmaksim.cloudfilestorage.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.denisovmaksim.cloudfilestorage.dto.response.NamedStreamDTOResponse;
import ru.denisovmaksim.cloudfilestorage.service.DownloadService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class DownloadControllerTest {

    @InjectMocks
    private DownloadController downloadController;

    @Mock
    private DownloadService downloadService;

    @Test
    void downloadZipFolderShouldReturnResponseEntity() {
        String path = "/folder";
        String fileName = "folder.zip";
        InputStream stream = new ByteArrayInputStream("data".getBytes());
        NamedStreamDTOResponse dto = new NamedStreamDTOResponse(fileName, 0, stream);

        Mockito.when(downloadService.getZipFolderAsStream(path)).thenReturn(dto);

        ResponseEntity<InputStreamResource> response = downloadController.downloadZipFolder(path);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=" + fileName,
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    void downloadFileShouldReturnResponseEntity() {
        String path = "/file.txt";
        String fileName = "file.txt";
        InputStream stream = new ByteArrayInputStream("file-content".getBytes());
        NamedStreamDTOResponse dto = new NamedStreamDTOResponse(fileName, 0, stream);

        Mockito.when(downloadService.getFileAsStream(path)).thenReturn(dto);

        ResponseEntity<InputStreamResource> response = downloadController.downloadFile(path);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=" + fileName,
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }


}
