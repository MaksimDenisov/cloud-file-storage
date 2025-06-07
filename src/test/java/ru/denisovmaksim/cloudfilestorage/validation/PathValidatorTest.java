package ru.denisovmaksim.cloudfilestorage.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathValidatorTest {
    private final PathValidator filenameValidator = new PathValidator(PathType.FILENAME);
    private final PathValidator filepathValidator = new PathValidator(PathType.FILEPATH);
    private final PathValidator dirValidator = new PathValidator(PathType.DIR);


    @ParameterizedTest()
    @ValueSource(strings = {
            "file.txt",
            "/file.txt",
            "folder/file.txt",
            "valid_folder/subfolder/file.txt",
            "Мирный, Шлюп/Чертеж.pdf"})
    public void testValidFilePath(String directoryPath) {
        assertTrue(filepathValidator.isValid(directoryPath, null),
                directoryPath + " expected valid directory path but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "<", ">", ":", "\"", "\\", "|", "?", "*",
    })
    public void testInvalidFilePath(String directoryPath) {
        assertFalse(filepathValidator.isValid(directoryPath, null),
                directoryPath + " expected invalid directory path but got valid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {"файл",
            "file.txt"})
    public void testValidFileName(String filename) {
        assertTrue(filenameValidator.isValid(filename, null),
                filename + " expected valid file name but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "<", ">", ":", "\"", "\\", "/", "|", "?", "*",
            "folder/file.txt"
    })
    public void testInvalidFileName(String filename) {
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
    public void testValidDirectoryPath(String directoryPath) {
        assertTrue(dirValidator.isValid(directoryPath, null),
                directoryPath + " expected valid directory path but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "<", ">", ":", "\"", "\\", "|", "?", "*",
            "folder_without_ending_slash",
    })
    public void testInvalidDirectoryPath(String directoryPath) {
        assertFalse(dirValidator.isValid(directoryPath, null),
                directoryPath + " expected invalid directory path but got valid");
    }
}
