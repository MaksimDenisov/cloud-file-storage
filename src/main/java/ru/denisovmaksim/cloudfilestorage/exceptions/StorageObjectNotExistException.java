package ru.denisovmaksim.cloudfilestorage.exceptions;

public class StorageObjectNotExistException extends RuntimeException {
    public StorageObjectNotExistException(Throwable cause) {
        super(cause);
    }
}
