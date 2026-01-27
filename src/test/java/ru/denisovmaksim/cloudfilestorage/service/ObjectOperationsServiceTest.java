package ru.denisovmaksim.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.storage.MinioDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.MinioMetadataAccessor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObjectOperationsServiceTest {

    @Mock
    private MinioMetadataAccessor minioMetadataAccessor;
    @Mock
    private MinioDataAccessor minioDataAccessor;

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
        when(minioMetadataAccessor.isExistByPrefix(USER_ID, "dir/")).thenReturn(false);

        objectOperationsService.createFolder("dir/");

        verify(minioMetadataAccessor).createPath(USER_ID, "dir/");
    }

    @Test
    @DisplayName("Should throw exception if directory exist.")
    void createDuplicateDirectory() {
        when(minioMetadataAccessor.isExistByPrefix(USER_ID, "dir/")).thenReturn(true);

        assertThrows(ObjectAlreadyExistException.class, () ->
                objectOperationsService.createFolder("dir/")
        );
    }



    @Test
    @DisplayName("Rename file should copy and delete.")
    void renameFileShouldCopyAndDeleteWhenNewNotExists() {
        when(minioMetadataAccessor.isExistByPrefix(USER_ID, "dir/new.txt")).thenReturn(false);

        objectOperationsService.renameFile("dir/old.txt", "new.txt");

        verify(minioDataAccessor).copyOneObject(USER_ID, "dir/old.txt", "dir/new.txt");
        verify(minioDataAccessor).deleteOneObject(USER_ID, "dir/old.txt");
    }

    @Test
    @DisplayName("If file last in folder and parent folder not exist should create it.")
    void deleteFileShouldDeleteAndCreateParentFolderIfMissing() {
        when(minioMetadataAccessor.isExistByPrefix(USER_ID, "dir/")).thenReturn(false);

        objectOperationsService.deleteFile("dir/file.txt");

        verify(minioDataAccessor).deleteObjects(USER_ID, "dir/file.txt");
        verify(minioMetadataAccessor).createPath(USER_ID, "dir/");
    }


    @Test
    @DisplayName("Rename folder should rename all files.")
    void renameFolderSuccess() {
        String currentPath = "docs/";
        String newFolderName = "newDocs";
        String newPath = "newDocs/";

        when(minioMetadataAccessor.isExistByPrefix(USER_ID, newPath)).thenReturn(false);
        when(minioDataAccessor.copyObjects(any(), any(), any())).thenReturn(5);

        objectOperationsService.renameFolder(currentPath, newFolderName);

        verify(minioDataAccessor).copyObjects(eq(USER_ID), eq(currentPath), anyString());
        verify(minioDataAccessor).deleteObjects(USER_ID, currentPath);
    }

    @Test
    @DisplayName("Rename empty folder.")
    void renameEmptyFolderSuccess() {
        String currentPath = "docs/";
        String newFolderName = "newFolder";
        String newPath = "newFolder/";

        when(minioMetadataAccessor.isExistByPrefix(USER_ID, newPath)).thenReturn(false);

        objectOperationsService.renameFolder(currentPath, newFolderName);

        verify(minioMetadataAccessor).createPath(USER_ID, newPath);
        verify(minioDataAccessor).deleteObjects(USER_ID, currentPath);
    }

    @Test
    @DisplayName("Rename folder should throw exception if same name is exist.")
    void renameFolderShouldThrowsExceptionIfFolderAlreadyExists() {
        String currentPath = "docs/folder/";
        String newFolderName = "existingFolder";
        String newPath = "docs/existingFolder/";

        when(minioMetadataAccessor.isExistByPrefix(USER_ID, newPath)).thenReturn(true);

        assertThrows(ObjectAlreadyExistException.class, () ->
                objectOperationsService.renameFolder(currentPath, newFolderName)
        );

        verify(minioDataAccessor, never()).copyObjects(any(), any(), any());
        verify(minioDataAccessor, never()).deleteObjects(any(), any());
    }

    @Test
    @DisplayName("If folder last in folder and parent folder not exist should create it.")
    void deleteNotLastFolderParentPathNotCreate() {
        String path = "docs/folder/";
        String parentPath = "docs/";

        when(minioMetadataAccessor.isExistByPrefix(USER_ID, parentPath)).thenReturn(false);

        objectOperationsService.deleteFolder(path);

        verify(minioDataAccessor).deleteObjects(USER_ID, path);
        verify(minioMetadataAccessor).createPath(USER_ID, parentPath);
    }

    @Test
    @DisplayName("If folder not last in folder and parent folder not exist shouldn't create path.")
    void deleteFolderParentPathNotCreateIfAlreadyExists() {
        String path = "photos/events/";
        String parentPath = "photos/";

        when(minioMetadataAccessor.isExistByPrefix(USER_ID, parentPath)).thenReturn(true);

        objectOperationsService.deleteFolder(path);

        verify(minioDataAccessor).deleteObjects(USER_ID, path);
        verify(minioMetadataAccessor, never()).createPath(any(), any());
    }
}
