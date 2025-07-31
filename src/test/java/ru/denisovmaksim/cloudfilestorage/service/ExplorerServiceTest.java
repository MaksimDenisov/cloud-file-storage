package ru.denisovmaksim.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExplorerServiceTest {

    @Mock
    private MinioFileStorage fileStorage;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private ExplorerService explorerService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUser() {
        when(securityService.getAuthUserId()).thenReturn(USER_ID);
    }

    @Test
    @DisplayName("Create directory.")
    void createDirectory() {
        when(fileStorage.isExist(USER_ID, "dir/")).thenReturn(false);

        explorerService.createFolder("", "dir/");

        verify(fileStorage).createPath(USER_ID, "dir/");
    }

    @Test
    @DisplayName("Should throw exception if directory exist.")
    void createDuplicateDirectory() {
        when(fileStorage.isExist(USER_ID, "dir/")).thenReturn(true);

        assertThrows(ObjectAlreadyExistException.class, () ->
                explorerService.createFolder("", "dir/")
        );
    }

    @Test
    @DisplayName("If directory exist should return list.")
    void getContentOfDirectory() {
        when(fileStorage.listObjectInfo(USER_ID, "dir/")).thenReturn(Optional.of(List.of()));

        List<StorageObjectDTO> result = explorerService.getFolder("dir/");
        assertNotNull(result);
    }

    @Test
    @DisplayName("If directory not exist should throw exception.")
    void getContentOfNotExistDirectoryShouldThrowNotFound() {
        when(fileStorage.listObjectInfo(USER_ID, "dir/")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> explorerService.getFolder("dir/"));
    }


    @Test
    @DisplayName("Rename file should copy and delete.")
    void renameFileShouldCopyAndDeleteWhenNewNotExists() {
        when(fileStorage.isExist(USER_ID, "dir/new.txt")).thenReturn(false);

        explorerService.renameFile("dir/", "old.txt", "new.txt");

        verify(fileStorage).copyOneObject(USER_ID, "dir/old.txt", "dir/new.txt");
        verify(fileStorage).deleteObjects(USER_ID, "dir/old.txt");
    }

    @Test
    @DisplayName("If file last in folder and parent folder not exist should create it.")
    void deleteFileShouldDeleteAndCreateParentFolderIfMissing() {
        when(fileStorage.isExist(USER_ID, "dir/")).thenReturn(false);

        explorerService.deleteFile("dir/", "file.txt");

        verify(fileStorage).deleteObjects(USER_ID, "dir/file.txt");
        verify(fileStorage).createPath(USER_ID, "dir/");
    }


    @Test
    @DisplayName("Rename folder should rename all files.")
    void renameFolderSuccess() {
        String currentPath = "docs/";
        String newFolderName = "newDocs";
        String newPath = "newDocs/";

        when(fileStorage.isExist(USER_ID, newPath)).thenReturn(false);
        when(fileStorage.copyObjects(any(), any(), any())).thenReturn(5);

        explorerService.renameFolder(currentPath, newFolderName);

        verify(fileStorage).copyObjects(eq(USER_ID), eq(currentPath), anyString());
        verify(fileStorage).deleteObjects(USER_ID, currentPath);
    }

    @Test
    @DisplayName("Rename empty folder.")
    void renameEmptyFolderSuccess() {
        String currentPath = "docs/";
        String newFolderName = "newFolder";
        String newPath = "newFolder/";

        when(fileStorage.isExist(USER_ID, newPath)).thenReturn(false);

        explorerService.renameFolder(currentPath, newFolderName);

        verify(fileStorage).createPath(USER_ID, newPath);
        verify(fileStorage).deleteObjects(USER_ID, currentPath);
    }

    @Test
    @DisplayName("Rename folder should throw exception if same name is exist.")
    void renameFolderShouldThrowsExceptionIfFolderAlreadyExists() {
        String currentPath = "docs/folder/";
        String newFolderName = "existingFolder";
        String newPath = "docs/existingFolder/";

        when(fileStorage.isExist(USER_ID, newPath)).thenReturn(true);

        assertThrows(ObjectAlreadyExistException.class, () ->
                explorerService.renameFolder(currentPath, newFolderName)
        );

        verify(fileStorage, never()).copyObjects(any(), any(), any());
        verify(fileStorage, never()).deleteObjects(any(), any());
    }

    @Test
    @DisplayName("If folder last in folder and parent folder not exist should create it.")
    void deleteNotLastFolderParentPathNotCreate() {
        String path = "docs/folder/";
        String parentPath = "docs/";

        when(fileStorage.isExist(USER_ID, parentPath)).thenReturn(false);

        explorerService.deleteFolder(path);

        verify(fileStorage).deleteObjects(USER_ID, path);
        verify(fileStorage).createPath(USER_ID, parentPath);
    }

    @Test
    @DisplayName("If folder not last in folder and parent folder not exist shouldn't create path.")
    void deleteFolderParentPathNotCreateIfAlreadyExists() {
        String path = "photos/events/";
        String parentPath = "photos/";

        when(fileStorage.isExist(USER_ID, parentPath)).thenReturn(true);

        explorerService.deleteFolder(path);

        verify(fileStorage).deleteObjects(USER_ID, path);
        verify(fileStorage, never()).createPath(any(), any());
    }
}
