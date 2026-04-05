package ru.denisovmaksim.cloudfilestorage.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.denisovmaksim.cloudfilestorage.exception.RootFolderException;
import ru.denisovmaksim.cloudfilestorage.exception.StoragePathException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirPathTest {
    @Test()
    @DisplayName("Throws StoragePathException for null path")
    void testNull() {
        assertThrows(StoragePathException.class, () -> DirPath.of(null));
    }

    @ParameterizedTest()
    @ValueSource(strings = {"", "  ", "/"})
    @DisplayName("Should recognize root directory for blank or slash values")
    void testValidRoot(String value) {
        DirPath path = DirPath.of(value);
        assertTrue(path.isRoot(), value + " expected valid root path but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {"folder"})
    @DisplayName("Non-root paths are not recognized as root")
    void testInvalidRoot(String value) {
        DirPath path = DirPath.of(value);
        assertFalse(path.isRoot(), "Root must contain spaces or one slash");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "folder<", "folder>", "folder:", "folder\"", "folder\\", "folder|", "folder?", "folder*",
            "../folder", "folder/..", "/../folder"
    })
    @DisplayName("Invalid directory paths throw StoragePathException")
    void testInvalidDir(String value) {
        assertThrows(StoragePathException.class, () -> DirPath.of(value));
    }

    @Test
    @DisplayName("")
    void testRootName() {
        DirPath path = DirPath.of("/");
        assertTrue(path.name().isEmpty());
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "folder", "folder/", "documents/folder", "documents/folder/"
    })
    @DisplayName("Invalid file paths throw StoragePathException")
    void testDirName(String value) {
        DirPath path = DirPath.of(value);
        String name = path.name();
        assertEquals("folder", name);
    }

    @Test
    @DisplayName("Rename operation works and validates root and path constraints")
    void testRenameTo() {
        assertThrows(RootFolderException.class, () -> DirPath.of("").renameTo("RenamedRoot"));
        assertThrows(StoragePathException.class, () -> DirPath.of("file.txt").renameTo(""));
        assertThrows(StoragePathException.class, () -> DirPath.of("file.txt").renameTo("/"));

        assertEquals("/", DirPath.of("folder").parent().value());
        assertEquals("newFolder/", DirPath.of("folder/")
                .renameTo("newFolder")
                .value());
    }

    @Test
    @DisplayName("Parent paths are correctly determined")
    void testParent() {
        assertThrows(RootFolderException.class, () -> DirPath.of("").parent().value());
        assertEquals("/", DirPath.of("folder").parent().value());
        assertEquals("folder/", DirPath.of("folder/folder").parent().value());
    }
}
