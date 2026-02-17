package ru.denisovmaksim.cloudfilestorage.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.denisovmaksim.cloudfilestorage.dto.response.StorageObjectDTOResponse;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.model.FileType;
import ru.denisovmaksim.cloudfilestorage.service.fixture.StorageFixture;
import ru.denisovmaksim.cloudfilestorage.storage.AbstractMinioIntegrationTest;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

@SpringJUnitConfig
@Testcontainers
@Import({ExplorerService.class, StorageDataAccessor.class, StorageMetadataAccessor.class, StorageFixture.class})
public class ExplorerServiceTest extends AbstractMinioIntegrationTest {
    @Autowired
    private ExplorerService explorerService;
    @MockBean
    private SecurityService securityService;
    @Autowired
    private StorageFixture fixture;

    @BeforeEach
    public void beforeEach() {
        Mockito.when(securityService.getAuthUserId())
                .thenReturn(StorageFixture.USER_ID);
        fixture.clearAll();
    }

    @Test
    @DisplayName("Returns empty list for empty root directory")
    void shouldReturnEmptyListWhenGetEmptyRootFolder() {
        List<StorageObjectDTOResponse> actual = explorerService.getFolder("");

        assertThat(actual)
                .isNotNull()
                .hasSize(0);
    }

    @Test
    @DisplayName("Throws NotFoundException for non-existing directory")
    void shouldThrowExceptionWhenFolderNotExist() {
        assertThatThrownBy(() -> explorerService.getFolder("folder/subfolder/"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("folder/subfolder/");
    }

    @Test
    @DisplayName("Returns direct children sorted (folders first, then files) with calculated folder size")
    void shouldReturnSortedListWhenGetFolder() {
        fixture.file("folder/subfolder1/anotherFolder/", "file.txt", "hello");
        fixture.file("folder/subfolder1/", "file.txt", "hello");
        fixture.file("folder/", "file.txt", "hello");
        fixture.folder("folder/subfolder2/");
        fixture.folder("folder/subfolder1/");
        fixture.folder("folder/subfolder1/anotherFolder/");

        List<StorageObjectDTOResponse> actual = explorerService.getFolder("folder/");

        assertThat(actual)
                .hasSize(3)
                .extracting(
                        StorageObjectDTOResponse::fullPath,
                        StorageObjectDTOResponse::name,
                        StorageObjectDTOResponse::type,
                        StorageObjectDTOResponse::size
                )
                .containsExactly(
                        tuple("folder/subfolder1/", "subfolder1", FileType.FOLDER, 2L),
                        tuple("folder/subfolder2/", "subfolder2", FileType.FOLDER, 0L),
                        tuple("folder/file.txt", "file.txt", FileType.UNKNOWN_FILE, 5L)
                );

    }
}
