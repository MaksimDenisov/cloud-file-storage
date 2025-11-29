package ru.denisovmaksim.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.denisovmaksim.cloudfilestorage.IntegrationTestConfiguration;
import ru.denisovmaksim.cloudfilestorage.dto.response.StorageObjectDTOResponse;
import ru.denisovmaksim.cloudfilestorage.fixture.StorageFixture;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest()
@ActiveProfiles("it")
@Import({IntegrationTestConfiguration.class, StorageFixture.class})
@Testcontainers
public class ObjectOperationsServiceIT {
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
    void shouldRenameFile() {
        storageFixture.file("", "file", "Content");

        objectOperationsService.renameFile("file", "new-file");

        List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
        assertThat(actual)
                .anySatisfy(dto -> {
                    assertThat(dto.getFullPath()).isEqualTo("new-file");
                    assertThat(dto.getName()).isEqualTo("new-file");
                });
        assertThat(actual)
                .noneSatisfy(dto -> {
                    assertThat(dto.getName()).isEqualTo("file");
                });
    }

    @Test
    void shouldRenameFileWithSamePrefixLonger() {
        storageFixture.file("", "file", "Content");

        objectOperationsService.renameFile("file", "file.txt");

        List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
        assertThat(actual)
                .anySatisfy(dto -> {
                    assertThat(dto.getFullPath()).isEqualTo("file.txt");
                    assertThat(dto.getName()).isEqualTo("file.txt");
                });
        assertThat(actual)
                .noneSatisfy(dto -> {
                    assertThat(dto.getName()).isEqualTo("file");
                });
    }
}
