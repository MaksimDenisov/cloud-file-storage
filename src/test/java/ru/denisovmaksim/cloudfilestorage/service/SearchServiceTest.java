package ru.denisovmaksim.cloudfilestorage.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denisovmaksim.cloudfilestorage.dto.FileType;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private MinioFileStorage fileStorage;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private SearchService searchService;

    @Test
    @DisplayName("search() should return sorted DTOs based on query")
    void searchShouldReturnSortedDTOs() {
        Long userId = 42L;
        String query = "report";
        String baseDir = "documents/reports/";
        String parentDir1 = baseDir + "2025/";
        String parentDir2 = baseDir + "2025/";

        List<StorageObjectDTO> expected = List.of(
                new StorageObjectDTO("documents/", "reports/", FileType.FOLDER, 2L),
                new StorageObjectDTO(parentDir1, "report1.txt", FileType.UNKNOWN_FILE, 1000L),
                new StorageObjectDTO(parentDir2, "report2.txt", FileType.UNKNOWN_FILE, 2000L)
        );

        List<StorageObjectInfo> mockInfos = List.of(
                new StorageObjectInfo(parentDir1 + "report1.txt", "report1.txt", false, 1000L),
                new StorageObjectInfo(parentDir2 + "report2.txt", "report2.txt", false, 2000L)
        );

        when(securityService.getAuthUserId()).thenReturn(userId);
        when(fileStorage.getDirectChildCount(userId, "documents/reports/")).thenReturn(2L);
        when(fileStorage.searchObjectInfo(userId, "", query)).thenReturn(mockInfos);

        List<StorageObjectDTO> actual = searchService.search(query);

        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(expected);
    }
}
