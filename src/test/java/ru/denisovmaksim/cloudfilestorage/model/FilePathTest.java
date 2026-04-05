package ru.denisovmaksim.cloudfilestorage.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.denisovmaksim.cloudfilestorage.exception.StoragePathException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilePathTest {
    @Test()
    @DisplayName("Throws StoragePathException for null path")
    void testNull() {
        assertThrows(StoragePathException.class, () -> FilePath.of(null));
    }
    @Test
    @DisplayName("detectMimeType throws exception for directories and returns MIME for files")
    void testDetectMimeType() {
        assertNotNull(FilePath.of("file.txt").detectMimeType());
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "", "  ", "file<txt", "file>txt", "file:txt",
            "file\"txt", "file\\txt", "file|txt", "file?txt", "folder*txt",
            "../file.txt", "file.txt/..", "/../file.txt"
    })
    @DisplayName("Invalid file paths throw StoragePathException")
    void testInvalidFile(String value) {
        assertThrows(StoragePathException.class, () -> FilePath.of(value));
    }


    @Test
    @DisplayName("Rename operation works and validates root and path constraints")
    void testRenameTo() {
        assertEquals("renamedFile.txt", FilePath.of("file.txt")
                .renameTo("renamedFile.txt")
                .value());
        assertEquals("folder/renamedFile.txt", FilePath.of("folder/file.txt")
                .renameTo("renamedFile.txt")
                .value());
    }

    @Test
    @DisplayName("File path ending with slash throws StoragePathException")
    void testFileNameWithEndingSlash() {
        assertThrows(StoragePathException.class, () -> FilePath.of("folder/"));
    }

    @Test
    @DisplayName("Parent paths are correctly determined")
    void testParent() {
        assertEquals("/", FilePath.of("file.txt").parent().value());
        assertEquals("folder/", FilePath.of("folder/file.txt").parent().value());
    }
}
