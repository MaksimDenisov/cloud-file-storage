package ru.denisovmaksim.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.denisovmaksim.cloudfilestorage.IntegrationTestConfiguration;
import ru.denisovmaksim.cloudfilestorage.dto.response.StorageObjectDTOResponse;
import ru.denisovmaksim.cloudfilestorage.service.fixture.StorageFixture;
import ru.denisovmaksim.cloudfilestorage.storage.AbstractMinioIntegrationTest;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@Testcontainers
@Import({ObjectOperationsService.class, ExplorerService.class,
        StorageDataAccessor.class, StorageMetadataAccessor.class,
        StorageFixture.class})
public class ObjectOperationsServiceIT extends AbstractMinioIntegrationTest {
    @Autowired
    private ExplorerService explorerService;

    @Autowired
    private ObjectOperationsService objectOperationsService;

    @Autowired
    private StorageFixture storageFixture;

    @MockBean
    private SecurityService securityService;

    @BeforeEach
    public void beforeEach() {
        Mockito.when(securityService.getAuthUserId())
                .thenReturn(StorageFixture.USER_ID);
        storageFixture.clearAll();
    }

    @Test
    @DisplayName("Rename file")
    void shouldRenameFile() {
        storageFixture.file("", "file", "Content");

        objectOperationsService.renameFile("file", "new-file");

        List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
        assertThat(actual)
                .anySatisfy(dto -> {
                    assertThat(dto.fullPath()).isEqualTo("new-file");
                    assertThat(dto.name()).isEqualTo("new-file");
                });
        assertThat(actual)
                .noneSatisfy(dto -> {
                    assertThat(dto.name()).isEqualTo("file");
                });
    }

    @Test
    @DisplayName("Rename file with name has same prefix but longer")
    void shouldRenameFileWithSamePrefixLonger() {
        storageFixture.file("", "file", "Content");

        objectOperationsService.renameFile("file", "file.txt");

        List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
        assertThat(actual)
                .anySatisfy(dto -> {
                    assertThat(dto.fullPath()).isEqualTo("file.txt");
                    assertThat(dto.name()).isEqualTo("file.txt");
                });
        assertThat(actual)
                .noneSatisfy(dto -> {
                    assertThat(dto.name()).isEqualTo("file");
                });
    }

    @Test
    @DisplayName("Rename file with name has same prefix but shorter")
    void shouldRenameFileWithSamePrefixShorter() {
        storageFixture.file("", "file.txt", "Content");

        objectOperationsService.renameFile("file.txt", "file");

        List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
        assertThat(actual)
                .anySatisfy(dto -> {
                    assertThat(dto.fullPath()).isEqualTo("file");
                    assertThat(dto.name()).isEqualTo("file");
                });
        assertThat(actual)
                .noneSatisfy(dto -> {
                    assertThat(dto.name()).isEqualTo("file.txt");
                });
    }
}
