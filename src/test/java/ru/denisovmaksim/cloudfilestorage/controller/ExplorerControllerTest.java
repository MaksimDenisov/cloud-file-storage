package ru.denisovmaksim.cloudfilestorage.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.dto.FileType;
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

    @Mock
    private Authentication authentication;

    @Test
    void addFolderShouldCreateDirectoryAndRedirect() {
        String path = "/test";
        String folderName = "new-folder";

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = explorerController.addFolder(folderName, path, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(explorerService).createDirectory(path, folderName);
        Mockito.verify(redirectAttributes).addAttribute("path", path);
    }

    @Test
    void getObjectsShouldAddModelAttributesAndReturnView() {
        String path = "/test";
        String username = "user";
        StorageObjectDTO firstDTO =
                new StorageObjectDTO("file1.txt", "file1.txt", FileType.UNKNOWN_FILE, 100L);
        StorageObjectDTO secondDTO =
                new StorageObjectDTO("file2.txt", "file2.txt", FileType.UNKNOWN_FILE, 100L);

        List<StorageObjectDTO> mockResults = List.of(firstDTO, secondDTO);

        Mockito.when(authentication.getName()).thenReturn(username);
        Mockito.when(explorerService.getContentOfDirectory(path)).thenReturn(mockResults);

        String viewName = explorerController.getObjects(model, authentication, path);

        assertEquals("explorer/file-explorer", viewName);
        Mockito.verify(model).addAttribute("username", username);
        Mockito.verify(model).addAttribute("breadcrumbs", PathLinksDTOMapper.toChainLinksFromPath(path));
        Mockito.verify(model).addAttribute("storageObjects", mockResults);
        Mockito.verify(model).addAttribute("currentPath", path);
    }

    @Test
    void renameFolderShouldCallServiceAndRedirect() {
        String parentPath = "/parent";
        String currentPath = "/parent/old";
        String newName = "new";

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = explorerController.renameFolder(parentPath, currentPath, newName, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(explorerService).renameFolder(currentPath, newName);
        Mockito.verify(redirectAttributes).addAttribute("path", parentPath);
    }

    @Test
    void deleteFolderShouldCallServiceAndRedirect() {
        String redirectPath = "/parent";
        String folderPath = "/parent/child";

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = explorerController.deleteFolder(redirectPath, folderPath, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(explorerService).deleteFolder(folderPath);
        Mockito.verify(redirectAttributes).addAttribute("path", redirectPath);
    }

    @Test
    void renameFileShouldCallServiceAndRedirect() {
        String parentPath = "/parent";
        String oldName = "file1.txt";
        String newName = "file2.txt";

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = explorerController.renameFile(parentPath, oldName, newName, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(explorerService).renameFile(parentPath, oldName, newName);
        Mockito.verify(redirectAttributes).addAttribute("path", parentPath);
    }

    @Test
    void deleteFileShouldCallServiceAndRedirect() {
        String parentPath = "/parent";
        String fileName = "file.txt";

        Mockito.when(redirectAttributes.addAttribute(Mockito.eq("path"), Mockito.anyString()))
                .thenReturn(redirectAttributes);

        String result = explorerController.deleteFile(parentPath, fileName, redirectAttributes);

        assertEquals("redirect:/", result);
        Mockito.verify(explorerService).deleteFile(parentPath, fileName);
        Mockito.verify(redirectAttributes).addAttribute("path", parentPath);
    }
}
