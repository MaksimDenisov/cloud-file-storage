package ru.denisovmaksim.cloudfilestorage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.denisovmaksim.cloudfilestorage.dto.response.StorageObjectDTOResponse;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.exception.ObjectAlreadyExistException;
import ru.denisovmaksim.cloudfilestorage.exception.RootFolderModificationException;
import ru.denisovmaksim.cloudfilestorage.service.fixture.StorageFixture;
import ru.denisovmaksim.cloudfilestorage.storage.AbstractMinioIntegrationTest;
import ru.denisovmaksim.cloudfilestorage.storage.StorageDataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

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

    @Nested
    @DisplayName("Create folder")
    class CreateFolder {
        @Test
        @DisplayName("successfully")
        void shouldCreateFolder() {
            objectOperationsService.createFolder("folder");

            List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
            assertThat(actual)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .containsExactlyInAnyOrder(
                            tuple("folder/", "folder")
                    );
        }

        @Test
        @DisplayName("throw exception if it exist")
        void shouldNotCreateFolderIfFolderExist() {
            storageFixture.folder("folder/");

            assertThatThrownBy(() -> objectOperationsService.createFolder("folder"))
                    .isInstanceOf(ObjectAlreadyExistException.class);
        }
    }

    @Nested
    @DisplayName("Rename file")
    class RenameFile {
        @Test
        @DisplayName("with different name")
        void shouldRenameFile() {
            storageFixture.file("", "file", "Content");

            objectOperationsService.renameFile("file", "new-file");

            List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
            assertThat(actual)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .containsExactlyInAnyOrder(
                            tuple("new-file", "new-file")
                    );
            assertThat(actual)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .doesNotContain(tuple("file", "file"));
        }

        @Test
        @DisplayName("with name has same prefix but longer")
        void shouldRenameFileWithSamePrefixLonger() {
            storageFixture.file("", "file", "Content");

            objectOperationsService.renameFile("file", "file.txt");

            List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
            assertThat(actual)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .containsExactlyInAnyOrder(
                            tuple("file.txt", "file.txt")
                    );
            assertThat(actual)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .doesNotContain(tuple("file", "file"));
        }

        @Test
        @DisplayName("with name has same prefix but shorter")
        void shouldRenameFileWithSamePrefixShorter() {
            storageFixture.file("", "file.txt", "Content");

            objectOperationsService.renameFile("file.txt", "file");

            List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
            assertThat(actual)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .containsExactlyInAnyOrder(
                            tuple("file", "file")
                    );
            assertThat(actual)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .doesNotContain(tuple("file.txt", "file.txt"));
        }

        @Test
        @DisplayName("to exist name")
        void shouldRenameFileWithSameName() {
            storageFixture.file("", "file.txt", "Content");
            storageFixture.file("", "file1.txt", "Content");

            assertThatThrownBy(() -> objectOperationsService.renameFile("file.txt", "file1.txt"))
                    .isInstanceOf(ObjectAlreadyExistException.class);
        }
    }

    @Nested
    @DisplayName("Rename folder")
    class RenameFolder {
        @Test
        @DisplayName("with different name")
        void shouldRenameFolder() {
            storageFixture.folder("folder/");
            storageFixture.file("folder/", "file.txt", "Content");

            objectOperationsService.renameFolder("folder", "renamed-folder");

            List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
            assertThat(actual)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .containsExactlyInAnyOrder(
                            tuple("renamed-folder/", "renamed-folder")
                    )
                    .doesNotContain(tuple("folder/", "folder"));

            assertThatThrownBy(() -> explorerService.getFolder("folder/"))
                    .isInstanceOf(NotFoundException.class);

            List<StorageObjectDTOResponse> actualNewContent = explorerService.getFolder("renamed-folder/");
            assertThat(actualNewContent)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .containsExactlyInAnyOrder(
                            tuple("renamed-folder/file.txt", "file.txt")
                    );
        }

        @Test
        @DisplayName("with name has same prefix but longer")
        void shouldRenameFolderWithSamePrefixLonger() {
            storageFixture.folder("folder/");
            storageFixture.file("folder/", "file.txt", "Content");

            objectOperationsService.renameFolder("folder", "folder2");

            List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
            assertThat(actual)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .containsExactlyInAnyOrder(
                            tuple("folder2/", "folder2")
                    )
                    .doesNotContain(tuple("folder/", "folder"));

            assertThatThrownBy(() -> explorerService.getFolder("folder/"))
                    .isInstanceOf(NotFoundException.class);

            List<StorageObjectDTOResponse> actualNewContent = explorerService.getFolder("folder2/");
            assertThat(actualNewContent)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .containsExactlyInAnyOrder(
                            tuple("folder2/file.txt", "file.txt")
                    );
        }

        @Test
        @DisplayName("with name has same prefix but shorter")
        void shouldRenameFolderWithSamePrefixShorter() {
            storageFixture.folder("folder-old/");
            storageFixture.file("folder-old/", "file.txt", "Content");

            objectOperationsService.renameFolder("folder-old", "folder");

            List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
            assertThat(actual)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .containsExactlyInAnyOrder(
                            tuple("folder/", "folder")
                    )
                    .doesNotContain(tuple("folder-old/", "folder-old"));

            assertThatThrownBy(() -> explorerService.getFolder("folder-old/"))
                    .isInstanceOf(NotFoundException.class);


            List<StorageObjectDTOResponse> actualNewContent = explorerService.getFolder("folder/");
            assertThat(actualNewContent)
                    .extracting(StorageObjectDTOResponse::fullPath, StorageObjectDTOResponse::name)
                    .containsExactlyInAnyOrder(
                            tuple("folder/file.txt", "file.txt")
                    );
        }

        @Test
        @DisplayName("to exist name")
        void shouldThrowIfRenameFolderWithSameName() {
            storageFixture.folder("folder/");
            storageFixture.folder("new-folder/");

            assertThatThrownBy(() -> objectOperationsService.renameFolder("folder", "new-folder"))
                    .isInstanceOf(ObjectAlreadyExistException.class);
        }


        @Test
        @DisplayName("to exist file name")
        void shouldThrowIfRenameFolderWithSameNameFile() {
            storageFixture.folder("folder/");
            storageFixture.file("", "file.txt", "Content");

            assertThatThrownBy(() -> objectOperationsService.renameFolder("folder", "file.txt"))
                    .isInstanceOf(ObjectAlreadyExistException.class);
        }

        @Test
        @DisplayName("throw exception if try rename root")
        void shouldThrowIfRenameRoot() {
            assertThatThrownBy(() -> objectOperationsService.renameFolder("", "root"))
                    .isInstanceOf(RootFolderModificationException.class);
        }
    }

    @Nested
    @DisplayName("Delete file")
    class DeleteFile {
        @Test
        @DisplayName("successfully")
        void shouldDeleteFile() {
            storageFixture.file("", "file.txt", "Content");

            objectOperationsService.deleteFile("file.txt");

            List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
            assertThat(actual).isEmpty();
        }

        @Test
        @DisplayName("throw exception if not exist")
        void shouldThrowIfNotExist() {
            assertThatThrownBy(() -> objectOperationsService.deleteFile("file.txt"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete folder")
    class DeleteFolder {

        @Test
        @DisplayName("successfully")
        void shouldDeleteFolder() {
            storageFixture.folder("folder/");
            storageFixture.file("folder/", "file.txt", "Content");

            objectOperationsService.deleteFolder("folder");

            List<StorageObjectDTOResponse> actual = explorerService.getFolder("/");
            assertThat(actual).isEmpty();

            assertThatThrownBy(() -> explorerService.getFolder("folder"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("throw exception if not exist")
        void shouldThrowIfNotExist() {
            assertThatThrownBy(() -> objectOperationsService.deleteFolder("folder"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("throw exception if try delete root")
        void shouldThrowIfRoot() {
            assertThatThrownBy(() -> objectOperationsService.deleteFolder(""))
                    .isInstanceOf(RootFolderModificationException.class);
        }
    }
}
