package ru.denisovmaksim.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ObjectOperationsServiceTest {

    @Mock
    private StorageMetadataAccessor storageMetadataAccessor;
    @Mock
    private StorageDataAccessor storageDataAccessor;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private ObjectOperationsService objectOperationsService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUser() {
        when(securityService.getAuthUserId()).thenReturn(USER_ID);
    }

    @Test
    @DisplayName("Create directory.")
    void createDirectory() {
        when(storageMetadataAccessor.exist(USER_ID, "dir/")).thenReturn(false);

        objectOperationsService.createFolder("dir/");

        verify(storageMetadataAccessor).createPath(USER_ID, "dir/");
    }

    @Test
    @DisplayName("Should throw exception if directory exist.")
    void createDuplicateDirectory() {
        when(storageMetadataAccessor.exist(USER_ID, "dir/")).thenReturn(true);

        assertThrows(ObjectAlreadyExistException.class, () ->
                objectOperationsService.createFolder("dir/")
        );
    }


    @Test
    @DisplayName("Rename file should copy and delete.")
    void renameFileShouldCopyAndDeleteWhenNewNotExists() {
        when(storageMetadataAccessor.exist(USER_ID, "dir/new.txt")).thenReturn(false);

        objectOperationsService.renameFile("dir/old.txt", "new.txt");

        verify(storageDataAccessor).copyOneObject(USER_ID, "dir/old.txt", "dir/new.txt");
        verify(storageDataAccessor).deleteOneObject(USER_ID, "dir/old.txt");
    }

    @Test
    @DisplayName("If file last in folder and parent folder not exist should create it.")
    void deleteFileShouldDeleteAndCreateParentFolderIfMissing() {
        when(storageMetadataAccessor.exist(USER_ID, "dir/")).thenReturn(false);
        when(storageMetadataAccessor.exist(USER_ID, "dir/file.txt")).thenReturn(true);

        objectOperationsService.deleteFile("dir/file.txt");

        verify(storageDataAccessor).deleteOneObject(USER_ID, "dir/file.txt");
        verify(storageMetadataAccessor).createPath(USER_ID, "dir/");
    }


    @Test
    @DisplayName("Rename folder should rename all files.")
    void renameFolderSuccess() {
        String currentPath = "docs/";
        String newFolderName = "newDocs";
        String newDirPath = "newDocs/";
        String newFilePath = "newDocs";

        when(storageMetadataAccessor.exist(USER_ID, newFilePath)).thenReturn(false);
        when(storageMetadataAccessor.exist(USER_ID, newDirPath)).thenReturn(false);
        when(storageDataAccessor.copyObjects(any(), any(), any())).thenReturn(5);

        objectOperationsService.renameFolder(currentPath, newFolderName);

        verify(storageDataAccessor).copyObjects(USER_ID, currentPath, newDirPath);
        verify(storageDataAccessor).deleteObjects(USER_ID, currentPath);
    }

    @Test
    @DisplayName("Rename empty folder.")
    void renameEmptyFolderSuccess() {
        String currentPath = "docs/";
        String newFolderName = "newFolder";
        String newPath = "newFolder/";

        when(storageMetadataAccessor.exist(USER_ID, newFolderName)).thenReturn(false);
        when(storageMetadataAccessor.exist(USER_ID, newPath)).thenReturn(false);

        objectOperationsService.renameFolder(currentPath, newFolderName);

        verify(storageMetadataAccessor).createPath(USER_ID, newPath);
        verify(storageDataAccessor).deleteObjects(USER_ID, currentPath);
    }

    @Test
    @DisplayName("Rename folder should throw exception if same name is exist.")
    void renameFolderShouldThrowsExceptionIfFolderAlreadyExists() {
        String currentPath = "docs/folder/";
        String newFolderName = "existingFolder";
        String newDirPath = "docs/existingFolder/";
        String newFilePath = "docs/existingFolder";


        when(storageMetadataAccessor.exist(USER_ID, newFilePath))
                .thenReturn(false);
        when(storageMetadataAccessor.exist(USER_ID, newDirPath))
                .thenReturn(true);

        assertThrows(ObjectAlreadyExistException.class, () ->
                objectOperationsService.renameFolder(currentPath, newFolderName)
        );

        verify(storageDataAccessor, never()).copyObjects(any(), any(), any());
        verify(storageDataAccessor, never()).deleteObjects(any(), any());
    }

    @Test
    @DisplayName("Rename folder should throw exception if file with same name is exist.")
    void renameFolderShouldThrowsExceptionIfFileWithSameAlreadyExists() {
        String currentPath = "docs/folder/";
        String newFolderName = "existingFolder";
        String newFilePath = "docs/existingFolder";

        when(storageMetadataAccessor.exist(USER_ID, newFilePath)).thenReturn(true);

        assertThrows(ObjectAlreadyExistException.class, () ->
                objectOperationsService.renameFolder(currentPath, newFolderName)
        );

        verify(storageDataAccessor, never()).copyObjects(any(), any(), any());
        verify(storageDataAccessor, never()).deleteObjects(any(), any());
    }


    @Test
    @DisplayName("If folder last in folder and parent folder not exist should create it.")
    void deleteNotLastFolderParentPathNotCreate() {
        String path = "docs/folder/";
        String parentPath = "docs/";

        when(storageMetadataAccessor.exist(USER_ID, path)).thenReturn(true);
        when(storageMetadataAccessor.exist(USER_ID, parentPath)).thenReturn(false);

        objectOperationsService.deleteFolder(path);

        verify(storageDataAccessor).deleteObjects(USER_ID, path);
        verify(storageMetadataAccessor).createPath(USER_ID, parentPath);
    }

    @Test
    @DisplayName("If folder not last in folder and parent folder not exist shouldn't create path.")
    void deleteFolderParentPathNotCreateIfAlreadyExists() {
        String path = "photos/events/";
        String parentPath = "photos/";

        when(storageMetadataAccessor.exist(USER_ID, path)).thenReturn(true);
        when(storageMetadataAccessor.exist(USER_ID, parentPath)).thenReturn(true);

        objectOperationsService.deleteFolder(path);

        verify(storageDataAccessor).deleteObjects(USER_ID, path);
        verify(storageMetadataAccessor, never()).createPath(any(), any());
    }
}
