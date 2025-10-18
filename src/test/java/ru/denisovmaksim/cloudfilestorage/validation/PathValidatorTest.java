package ru.denisovmaksim.cloudfilestorage.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathValidatorTest {
    private final PathValidator filenameValidator = new PathValidator(PathType.NAME);
    private final PathValidator filepathValidator = new PathValidator(PathType.FILEPATH);
    private final PathValidator dirValidator = new PathValidator(PathType.DIR);


    @ParameterizedTest()
    @ValueSource(strings = {
            "file.txt",
            "/file.txt",
            "folder/file.txt",
            "valid_folder/subfolder/file.txt",
            "Мирный, Шлюп/Чертеж.pdf"})
    void testValidFilePath(String directoryPath) {
        assertTrue(filepathValidator.isValid(directoryPath, null),
                directoryPath + " expected valid directory path but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "<", ">", ":", "\"", "\\", "|", "?", "*",
    })
    void testInvalidFilePath(String directoryPath) {
        assertFalse(filepathValidator.isValid(directoryPath, null),
                directoryPath + " expected invalid directory path but got valid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {"файл",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!$(),-.^_` ",
            "!", "$", "(", ")", ",", "-", ".", "^", "_", "`", " ",
            "file.txt"})
    void testValidName(String filename) {
        assertTrue(filenameValidator.isValid(filename, null),
                filename + " expected valid file name but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "<", ">", ":", "\"", "\\", "/", "|", "?", "*",
            "#", "%", "[", "]", "{", "}", "~", "'", "\"", ";"
    })
    void testInvalidName(String filename) {
        assertFalse(filenameValidator.isValid(filename, null),
                filename + " expected invalid file name but got valid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "",
            "/",
            "folder/",
            "valid_folder/subfolder/",
            "valid, folder/subfolder/",
            "Мирный, Шлюп/"})
    void testValidDirectoryPath(String directoryPath) {
        assertTrue(dirValidator.isValid(directoryPath, null),
                directoryPath + " expected valid directory path but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "<", ">", ":", "\"", "\\", "|", "?", "*",
            "../", "/../", "/..",
            "folder_without_ending_slash",
    })
    void testInvalidDirectoryPath(String directoryPath) {
        assertFalse(dirValidator.isValid(directoryPath, null),
                directoryPath + " expected invalid directory path but got valid");
    }
}
