package ru.denisovmaksim.cloudfilestorage.exception;

public class RootFolderException extends RuntimeException {
    public RootFolderException() {
    }

    public RootFolderException(String message) {
        super(message);
    }

    public RootFolderException(String message, Throwable cause) {
        super(message, cause);
    }

    public RootFolderException(Throwable cause) {
        super(cause);
    }

    public RootFolderException(String message, Throwable cause,
                               boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
