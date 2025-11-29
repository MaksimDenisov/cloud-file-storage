package ru.denisovmaksim.cloudfilestorage.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denisovmaksim.cloudfilestorage.dto.response.StorageObjectDTOResponse;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.storage.MinioMetadataAccessor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExplorerServiceTest {

    @Mock
    private MinioMetadataAccessor minioMetadataAccessor;
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
    @DisplayName("If directory exist should return list.")
    void getContentOfDirectory() {
        when(minioMetadataAccessor.listObjectInfo(USER_ID, "dir/")).thenReturn(Optional.of(List.of()));

        List<StorageObjectDTOResponse> result = explorerService.getFolder("dir/");
        assertNotNull(result);
    }

    @Test
    @DisplayName("If directory not exist should throw exception.")
    void getContentOfNotExistDirectoryShouldThrowNotFound() {
        when(minioMetadataAccessor.listObjectInfo(USER_ID, "dir/")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> explorerService.getFolder("dir/"));
    }

}
