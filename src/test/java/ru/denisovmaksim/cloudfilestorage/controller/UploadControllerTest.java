package ru.denisovmaksim.cloudfilestorage.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.dto.request.UploadFileDTORequest;
import ru.denisovmaksim.cloudfilestorage.service.UploadService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class UploadControllerTest {
    @InjectMocks
    private UploadController uploadController;
    @Mock
    private UploadService uploadService;
    @Mock
    private RedirectAttributes redirectAttributes;

    @Test
    void uploadFileShouldRedirectToRootWhenFileIsEmpty() {
        String path = "/folder";
        MultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        Mockito.when(redirectAttributes.addAttribute(eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = uploadController.uploadFile(path, file, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(redirectAttributes).addFlashAttribute("flashType", "danger");
        Mockito.verify(redirectAttributes).addFlashAttribute("flashMsg", "Please select a file to upload.");
    }

    @Test
    void uploadFileShouldUploadAndRedirect() {
        String path = "/folder";
        MultipartFile file = new MockMultipartFile("file", "name.txt", "text/plain", "data".getBytes());

        Mockito.when(redirectAttributes.addAttribute(eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = uploadController.uploadFile(path, file, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(uploadService).uploadFile(eq(path), any(UploadFileDTORequest.class));
    }

    @Test
    void uploadFolderShouldUploadAllFiles() {
        String path = "/folder";
        List<MultipartFile> files = List.of(
                new MockMultipartFile("files", "f1.txt", "text/plain", "1".getBytes()),
                new MockMultipartFile("files", "f2.txt", "text/plain", "2".getBytes())
        );

        Mockito.when(redirectAttributes.addAttribute(eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = uploadController.uploadFolder(path, files, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(uploadService).uploadFolder(path, files);
    }
}
