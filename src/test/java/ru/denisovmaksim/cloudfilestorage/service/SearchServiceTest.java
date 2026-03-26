package ru.denisovmaksim.cloudfilestorage.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.denisovmaksim.cloudfilestorage.model.FileType;
import ru.denisovmaksim.cloudfilestorage.dto.response.StorageObjectDTOResponse;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private StorageMetadataAccessor metadataAccessor;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private SearchService searchService;

    @Test
    @DisplayName("search() should return sorted DTOs based on query")
    void searchShouldReturnSortedDTOs() {
        List<StorageObjectInfo> mockInfos = List.of(
                new StorageObjectInfo("documents/reports/2025/report1.txt", "report1.txt", false, 1000L),
                new StorageObjectInfo("documents/reports/2026/report2.txt", "report2.txt", false, 2000L)
        );

        when(securityService.getAuthUserId()).thenReturn(1L);
        when(metadataAccessor.findObjectInfosBySubstring(1L, "", "report")).thenReturn(mockInfos);
        List<StorageObjectDTOResponse> actual = searchService.search("report");

        assertThat(actual)
                .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name,
                        StorageObjectDTOResponse::type, StorageObjectDTOResponse::size)
                .containsExactlyInAnyOrder(
                        tuple("documents/reports/", "reports", FileType.FOLDER, 0L),
                        tuple("documents/reports/2025/report1.txt", "report1.txt", FileType.UNKNOWN_FILE, 1000L),
                        tuple("documents/reports/2026/report2.txt", "report2.txt", FileType.UNKNOWN_FILE, 2000L)
                );
    }
}
