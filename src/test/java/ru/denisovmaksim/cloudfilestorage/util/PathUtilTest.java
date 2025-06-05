package ru.denisovmaksim.cloudfilestorage.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.denisovmaksim.cloudfilestorage.util.PathUtil.getBaseName;
import static ru.denisovmaksim.cloudfilestorage.util.PathUtil.getParentDirName;
import static ru.denisovmaksim.cloudfilestorage.util.PathUtil.isDir;
import static ru.denisovmaksim.cloudfilestorage.util.PathUtil.isRoot;
import static ru.denisovmaksim.cloudfilestorage.util.PathUtil.isValid;
import static ru.denisovmaksim.cloudfilestorage.util.PathUtil.normalize;

class PathUtilTest {

    @Test
    void testNormalize() {
        assertEquals("folder/file.txt", normalize("folder//file.txt"));
        assertEquals("folder/file.txt", normalize("  folder/file.txt  "));
    }

    @Test
    void testIsRoot() {
        assertTrue(isRoot(""), "Empty string is root");
        assertTrue(isRoot("/"), "Single slash is root");
        assertFalse(isRoot("/folder"));
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "", "/", "folder/", "folder/file.txt"
    })
    void testIsValid(String path) {
        assertTrue(isValid(path));
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "<", ">", ":", "\"", "\\", "|", "?", "*",
    })
    void isValidShouldReturnFalseForPathWithIllegalCharacters(String path) {
        assertFalse(isValid(path),
                path + " expected invalid path but got valid");
    }

    @Test
    void isDirShouldReturnTrueIfPathEndsWithSlash() {
        assertTrue(isDir("folder/subfolder/"));
    }

    @Test
    void isDirShouldReturnFalseIfPathDoesNotEndWithSlash() {
        assertFalse(isDir("folder/file.txt"));
    }

    @Test
    void getNameShouldReturnLastSegmentForFile() {
        assertEquals("file.txt", getBaseName("folder/subfolder/file.txt"));
    }

    @Test
    void getNameShouldReturnLastSegmentForDirectory() {
        assertEquals("subfolder", getBaseName("folder/subfolder/"));
    }

    @Test
    void getParentDirNameShouldReturnParentPathWithSlash() {
        assertEquals("folder/subfolder/", getParentDirName("folder/subfolder/file.txt"));
        assertEquals("", getParentDirName("folder/"));
        assertEquals("", getParentDirName("file.txt"));
    }
}
