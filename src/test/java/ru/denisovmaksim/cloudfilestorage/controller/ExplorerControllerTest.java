package ru.denisovmaksim.cloudfilestorage.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.model.FileType;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.mapper.PathLinksDTOMapper;
import ru.denisovmaksim.cloudfilestorage.service.ExplorerService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ExplorerControllerTest {

    @InjectMocks
    private ExplorerController explorerController;

    @Mock
    private ExplorerService explorerService;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Model model;

    @Test
    void addFolderShouldCreateDirectoryAndRedirect() {
        String path = "/test";
        String folderName = "new-folder";

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = explorerController.addFolder(folderName, path, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(explorerService).createFolder(path + folderName);
        Mockito.verify(redirectAttributes).addAttribute("path", path);
    }

    @Test
    void getObjectsShouldAddModelAttributesAndReturnView() {
        String path = "/test";
        StorageObjectDTO firstDTO =
                new StorageObjectDTO("file1.txt", "file1.txt", FileType.UNKNOWN_FILE, 100L);
        StorageObjectDTO secondDTO =
                new StorageObjectDTO("file2.txt", "file2.txt", FileType.UNKNOWN_FILE, 100L);

        List<StorageObjectDTO> mockResults = List.of(firstDTO, secondDTO);

        Mockito.when(explorerService.getFolder(path)).thenReturn(mockResults);

        String viewName = explorerController.getObjects(model, path);

        assertEquals("explorer/content", viewName);
        Mockito.verify(model).addAttribute("breadcrumbs", PathLinksDTOMapper.toChainLinksFromPath(path));
        Mockito.verify(model).addAttribute("storageObjects", mockResults);
        Mockito.verify(model).addAttribute("currentPath", path);
    }

    @Test
    void renameFolderShouldCallServiceAndRedirect() {
        String parentPath = "/parent/";
        String currentPath = "/parent/old";
        String newName = "new";

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = explorerController.renameFolder(currentPath, newName, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(explorerService).renameFolder(currentPath, newName);
        Mockito.verify(redirectAttributes).addAttribute("path", parentPath);
    }

    @Test
    void deleteFolderShouldCallServiceAndRedirect() {
        String redirectPath = "/parent/";
        String folderPath = "/parent/child";

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = explorerController.deleteFolder(folderPath, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(explorerService).deleteFolder(folderPath);
        Mockito.verify(redirectAttributes).addAttribute("path", redirectPath);
    }

    @Test
    void renameFileShouldCallServiceAndRedirect() {
        String filepath = "/parent/file1.txt";
        String newName = "file2.txt";

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = explorerController.renameFile(filepath, newName, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(explorerService).renameFile(filepath, newName);
        Mockito.verify(redirectAttributes).addAttribute("path", "/parent/");
    }

    @Test
    void deleteFileShouldCallServiceAndRedirect() {
        String filePath = "/parent/file.txt";

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = explorerController.deleteFile(filePath, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(explorerService).deleteFile(filePath);
    }
}
