package ru.denisovmaksim.cloudfilestorage.exception;

public class RootFolderModificationException extends RuntimeException {
    public RootFolderModificationException() {
    }

    public RootFolderModificationException(String message) {
        super(message);
    }

    public RootFolderModificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RootFolderModificationException(Throwable cause) {
        super(cause);
    }

    public RootFolderModificationException(String message, Throwable cause,
                                           boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
