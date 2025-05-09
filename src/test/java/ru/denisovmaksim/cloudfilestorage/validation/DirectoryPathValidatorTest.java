package ru.denisovmaksim.cloudfilestorage.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryPathValidatorTest {
    private final DirectoryPathValidator validator = new DirectoryPathValidator();

    @ParameterizedTest()
    @ValueSource(strings = {
            "",
            "/",
            "folder/",
            "valid_folder/subfolder/",
            "valid, folder/subfolder/",
            "Мирный, Шлюп/"})
    public void testValidDirectoryPath(String directoryPath) {
        assertTrue(validateDirectoryPath(directoryPath),
                directoryPath + " expected valid directory path but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "<", ">", ":", "\"", "\\", "|", "?", "*",
            "folder_without_ending_slash",
    })
    public void testInvalidDirectoryPath(String directoryPath) {
        assertFalse(validateDirectoryPath(directoryPath),
                directoryPath + " expected invalid directory path but got valid");
    }

    private boolean validateDirectoryPath(String directoryPath) {
        return validator.isValid(directoryPath, null);
    }
}
