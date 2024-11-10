package ru.denisovmaksim.cloudfilestorage.config;

public class ValidationConstants {
    public static final String FILENAME_VALIDATION_REGEXP = "^[^/\\\\:*?\"<>|]+$";
    public static final String ERROR_MSG_FILENAME_INVALID = "Filename must not contains / \\ : * ? \\ \" < > | ";
    public static final String PATH_VALIDATION_REGEXP = "^([^\\\\:*?\"<>|]*\\/)?$";
    public static final String ERROR_MSG_PATH_INVALID_CHARACTERS = "Not valid path";
}
