package ru.denisovmaksim.cloudfilestorage.exceptions;

public class StorageObjectNotFoundException extends RuntimeException {

    private final String path;

    public StorageObjectNotFoundException(String path) {
        super(path + " was not found");
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
