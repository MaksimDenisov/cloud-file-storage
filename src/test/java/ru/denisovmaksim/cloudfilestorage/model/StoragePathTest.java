package ru.denisovmaksim.cloudfilestorage.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.denisovmaksim.cloudfilestorage.exception.RootFolderException;
import ru.denisovmaksim.cloudfilestorage.exception.StoragePathException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoragePathTest {

    @Test()
    @DisplayName("Throws StoragePathException for null path")
    void testNull() {
        assertThrows(StoragePathException.class, () -> StoragePath.dir(null));
        assertThrows(StoragePathException.class, () -> StoragePath.file(null));
    }

    @ParameterizedTest()
    @ValueSource(strings = {"", "  ", "/"})
    @DisplayName("Throws StoragePathException for null path")
    void testValidRoot(String value) {
        StoragePath path = StoragePath.dir(value);
        assertTrue(path.isRoot(), value + " expected valid root path but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {"folder"})
    @DisplayName("Non-root paths are not recognized as root")
    void testInvalidRoot(String value) {
        StoragePath path = StoragePath.dir(value);
        assertFalse(path.isRoot(), "Root must contain spaces or one slash");
    }

    @ParameterizedTest()
    @ValueSource(strings = {"", "  ", "/", "folder", "folder/", "folder///"})
    @DisplayName("Valid directory paths are recognized as directories")
    void testValidDir(String value) {
        StoragePath path = StoragePath.dir(value);
        assertTrue(path.isDir(), value + " expected valid path but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "folder<", "folder>", "folder:", "folder\"", "folder\\", "folder|", "folder?", "folder*",
            "../folder", "folder/..", "/../folder"
    })
    @DisplayName("Invalid directory paths throw StoragePathException")
    void testInvalidDir(String value) {
        assertThrows(StoragePathException.class, () -> StoragePath.dir(value));
    }

    @ParameterizedTest()
    @ValueSource(strings = {"file.txt", "folder/file.txt", "folder///file.txt"})
    @DisplayName("Valid file paths are recognized as files")
    void testValidFile(String value) {
        StoragePath path = StoragePath.file(value);
        assertTrue(path.isFile(), value + " expected valid path but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "", "  ", "file<txt", "file>txt", "file:txt",
            "file\"txt", "file\\txt", "file|txt", "file?txt", "folder*txt",
            "../file.txt", "file.txt/..", "/../file.txt"
    })
    @DisplayName("Invalid file paths throw StoragePathException")
    void testInvalidFile(String value) {
        assertThrows(StoragePathException.class, () -> StoragePath.file(value));
    }

    @Test
    @DisplayName("Invalid file paths throw StoragePathException")
    void testRootName() {
        StoragePath path = StoragePath.dir("");
        assertTrue(path.name().isEmpty());
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "folder", "folder/", "documents/folder", "documents/folder/"
    })
    @DisplayName("Invalid file paths throw StoragePathException")
    void testDirName(String value) {
        StoragePath path = StoragePath.dir(value);
        String name = path.name();
        assertEquals("folder", name);
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "file.txt", "folder/file.txt", "documents/folder/file.txt"
    })
    @DisplayName("Invalid file paths throw StoragePathException")
    void testFileName(String value) {
        StoragePath path = StoragePath.file(value);
        String name = path.name();
        assertTrue(path.isFile(), value + " expected but got " + name);
    }

    @Test
    @DisplayName("File path ending with slash throws StoragePathException")
    void testFileNameWithEndingSlash() {
        assertThrows(StoragePathException.class, () -> StoragePath.file("folder/"));
    }

    @Test
    @DisplayName("Parent paths are correctly determined")
    void testParent() {
        assertThrows(RootFolderException.class, () -> StoragePath.dir("").parent().value());
        assertEquals("/", StoragePath.dir("folder").parent().value());
        assertEquals("/", StoragePath.file("file.txt").parent().value());
        assertEquals("folder/", StoragePath.dir("folder/folder").parent().value());
        assertEquals("folder/", StoragePath.file("folder/file.txt").parent().value());
    }

    @Test
    @DisplayName("Rename operation works and validates root and path constraints")
    void testRenameTo() {
        assertThrows(RootFolderException.class, () -> StoragePath.dir("").renameTo("RenamedRoot"));
        assertThrows(StoragePathException.class, () -> StoragePath.dir("file.txt").renameTo(""));
        assertThrows(StoragePathException.class, () -> StoragePath.dir("file.txt").renameTo("/"));

        assertEquals("/", StoragePath.dir("folder").parent().value());
        assertEquals("renamedFile.txt", StoragePath.file("file.txt")
                .renameTo("renamedFile.txt")
                .value());
        assertEquals("newFolder/", StoragePath.dir("folder/")
                .renameTo("newFolder")
                .value());
        assertEquals("folder/renamedFile.txt", StoragePath.file("folder/file.txt")
                .renameTo("renamedFile.txt")
                .value());
    }

    @Test
    @DisplayName("detectMimeType throws exception for directories and returns MIME for files")
    void testDetectMimeType() {
        assertThrows(StoragePathException.class, () -> StoragePath.dir("folder").detectMimeType());
        assertNotNull(StoragePath.file("file.txt").detectMimeType());
    }

}
