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
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.storage.FileObject;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private MinioFileStorage fileStorage;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private FileService fileService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUser() {
        when(securityService.getAuthUserId()).thenReturn(USER_ID);
    }

    @Test
    @DisplayName("Create directory.")
    void createDirectory() {
        when(fileStorage.isExist(USER_ID, "dir/")).thenReturn(false);

        fileService.createDirectory("", "dir/");

        verify(fileStorage).createPath(USER_ID, "dir/");
    }

    @Test
    @DisplayName("Should throw exception if directory exist.")
    void createDuplicateDirectory() {
        when(fileStorage.isExist(USER_ID, "dir/")).thenReturn(true);

        assertThrows(ObjectAlreadyExistException.class, () ->
                fileService.createDirectory("", "dir/")
        );
    }

    @Test
    @DisplayName("If directory exist should return list.")
    void getContentOfDirectory() {
        when(fileStorage.listObjectInfo(USER_ID, "dir/")).thenReturn(Optional.of(List.of()));

        List<StorageObjectDTO> result = fileService.getContentOfDirectory("dir/");
        assertNotNull(result);
    }

    @Test
    @DisplayName("If directory not exist should throw exception.")
    void getContentOfNotExistDirectoryShouldThrowNotFound() {
        when(fileStorage.listObjectInfo(USER_ID, "dir/")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> fileService.getContentOfDirectory("dir/"));
    }

    @Test
    @DisplayName("Upload file should save it to storage.")
    void uploadFileShouldSaveWhenNotExists() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.txt");
        when(fileStorage.isExist(USER_ID, "dir/file.txt")).thenReturn(false);

        fileService.uploadFile("dir/", file);

        verify(fileStorage).saveObject(USER_ID, "dir/", file);
    }

    @Test
    @DisplayName("Upload file should save it to storage.")
    void uploadFileShouldThrowWhenExists() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.txt");
        when(fileStorage.isExist(USER_ID, "dir/file.txt")).thenReturn(true);

        assertThrows(ObjectAlreadyExistException.class, () -> fileService.uploadFile("dir/", file));
    }

    @Test
    @DisplayName("Rename file should copy and delete.")
    void renameFileShouldCopyAndDeleteWhenNewNotExists() {
        when(fileStorage.isExist(USER_ID, "dir/new.txt")).thenReturn(false);

        fileService.renameFile("dir/", "old.txt", "new.txt");

        verify(fileStorage).copyOneObject(USER_ID, "dir/old.txt", "dir/new.txt");
        verify(fileStorage).deleteObjects(USER_ID, "dir/old.txt");
    }

    @Test
    @DisplayName("If file last in folder and parent folder not exist should create it.")
    void deleteFileShouldDeleteAndCreateParentFolderIfMissing() {
        when(fileStorage.isExist(USER_ID, "dir/")).thenReturn(false);

        fileService.deleteFile("dir/", "file.txt");

        verify(fileStorage).deleteObjects(USER_ID, "dir/file.txt");
        verify(fileStorage).createPath(USER_ID, "dir/");
    }

    @Test
    @DisplayName("Get file should return stream.")
    void getFileShouldReturnStreamDTO() {
        InputStream stream = new ByteArrayInputStream("test".getBytes());

        when(fileStorage.getObject(USER_ID, "dir/file.txt"))
                .thenReturn(new FileObject("dir/file.txt", stream));

        NamedStreamDTO result = fileService.getFileAsStream("dir/", "file.txt");
        assertEquals("file.txt", java.net.URLDecoder.decode(result.getName(), java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Get folder should return zipped stream.")
    void getZipFolderAsStreamShouldReturnZippedStreamDTO() {
        InputStream stream = new ByteArrayInputStream("test".getBytes());
        // when(securityService.getAuthUserId()).thenReturn(USER_ID);
        when(fileStorage.getObjects(USER_ID, "dir/")).thenReturn(List.of(
                new FileObject("dir/file.txt", stream)
        ));

        NamedStreamDTO result = fileService.getZipFolderAsStream("dir/");
        assertTrue(result.getName().endsWith(".zip"));
    }

    @Test
    @DisplayName("Get folder should return zipped stream.")
    void uploadFolderShouldSaveEachFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("folder/file.txt");
        List<MultipartFile> files = List.of(file);

        when(fileStorage.isExist(USER_ID, "folder")).thenReturn(false);

        fileService.uploadFolder("", files);

        verify(fileStorage).saveObject(USER_ID, "", file);
    }

    @Test
    @DisplayName("Rename folder should rename all files.")
    void renameFolderSuccess() {
        String currentPath = "docs/oldFolder/";
        String newFolderName = "newFolder";
        String newPath = "docs/newFolder/";

        when(fileStorage.isExist(USER_ID, newPath)).thenReturn(false);

        fileService.renameFolder(currentPath, newFolderName);

        verify(fileStorage).copyObjects(USER_ID, currentPath, newPath);
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
                fileService.renameFolder(currentPath, newFolderName)
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

        fileService.deleteFolder(path);

        verify(fileStorage).deleteObjects(USER_ID, path);
        verify(fileStorage).createPath(USER_ID, parentPath);
    }

    @Test
    @DisplayName("If folder not last in folder and parent folder not exist shouldn't create path.")
    void deleteFolderParentPathNotCreateIfAlreadyExists() {
        String path = "photos/events/";
        String parentPath = "photos/";

        when(fileStorage.isExist(USER_ID, parentPath)).thenReturn(true);

        fileService.deleteFolder(path);

        verify(fileStorage).deleteObjects(USER_ID, path);
        verify(fileStorage, never()).createPath(any(), any());
    }
}
