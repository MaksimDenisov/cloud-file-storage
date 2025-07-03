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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.service.TransferService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @InjectMocks
    private TransferController transferController;

    @Mock
    private TransferService transferService;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Test
    void downloadZipFolderShouldReturnResponseEntity() {
        String path = "/folder";
        String fileName = "folder.zip";
        InputStream stream = new ByteArrayInputStream("data".getBytes());
        NamedStreamDTO dto = new NamedStreamDTO(fileName, stream);

        Mockito.when(transferService.getZipFolderAsStream(path)).thenReturn(dto);

        ResponseEntity<InputStreamResource> response = transferController.downloadZipFolder(path);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=" + fileName,
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    void downloadFileShouldReturnResponseEntity() {
        String path = "/file.txt";
        String fileName = "file.txt";
        InputStream stream = new ByteArrayInputStream("file-content".getBytes());
        NamedStreamDTO dto = new NamedStreamDTO(fileName, stream);

        Mockito.when(transferService.getFileAsStream(path)).thenReturn(dto);

        ResponseEntity<InputStreamResource> response = transferController.downloadFile(path);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=" + fileName,
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    void uploadFileShouldRedirectToRootWhenFileIsEmpty() {
        String path = "/folder";
        MultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = transferController.uploadFile(path, file, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(redirectAttributes).addFlashAttribute("flashType", "danger");
        Mockito.verify(redirectAttributes).addFlashAttribute("flashMsg", "Please select a file to upload.");
    }

    @Test
    void uploadFileShouldUploadAndRedirect() {
        String path = "/folder";
        MultipartFile file = new MockMultipartFile("file", "name.txt", "text/plain", "data".getBytes());

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = transferController.uploadFile(path, file, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(transferService).uploadFile(path, file);
    }

    @Test
    void uploadFolderShouldUploadAllFiles() {
        String path = "/folder";
        List<MultipartFile> files = List.of(
                new MockMultipartFile("files", "f1.txt", "text/plain", "1".getBytes()),
                new MockMultipartFile("files", "f2.txt", "text/plain", "2".getBytes())
        );

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = transferController.uploadFolder(path, files, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(transferService).uploadFolder(path, files);
    }
}
