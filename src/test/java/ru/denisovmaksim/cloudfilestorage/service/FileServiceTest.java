package ru.denisovmaksim.cloudfilestorage.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denisovmaksim.cloudfilestorage.exception.StorageObjectNotFoundException;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {
    @InjectMocks
    private FileService fileService;

    @Mock
    private MinioFileStorage fileStorage;

    @Mock
    private SecurityService securityService;

    @Test
    @DisplayName("Should create an empty folder")
    void createFolderShouldCreateEmptyPath() {
        fileService.createFolder("", "folder");
        verify(fileStorage).createPath(0L, "folder/");
    }

    @Test
    @DisplayName("Should create an empty folder")
    void getContentOfDirectoryShouldThrowExceptionWhenDirectoryNotFound() {
        Long userId = 1L;
        String path = "/missing";
        when(securityService.getAuthUserId()).thenReturn(userId);
        when(fileStorage.getPathContent(userId, path)).thenReturn(Optional.empty());

        assertThrows(StorageObjectNotFoundException.class, () ->
                fileService.getContentOfDirectory(path)
        );
    }
}
