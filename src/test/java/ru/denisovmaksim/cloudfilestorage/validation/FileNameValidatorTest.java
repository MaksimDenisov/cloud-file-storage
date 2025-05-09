package ru.denisovmaksim.cloudfilestorage.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FileNameValidatorTest {
    private final FileNameValidator validator = new FileNameValidator();

    @ParameterizedTest()
    @ValueSource(strings = {"файл",
            "file.txt"})
    public void testValidFileName(String filename) {
        assertTrue(validateDirectoryPath(filename),
                filename + " expected valid file name but got invalid");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "<", ">", ":", "\"", "\\", "/", "|", "?", "*",
            "folder/file.txt"
    })
    public void testInvalidFileName(String filename) {
        assertFalse(validateDirectoryPath(filename),
                filename + " expected invalid file name but got valid");
    }

    private boolean validateDirectoryPath(String filename) {
        return validator.isValid(filename, null);
    }
}
