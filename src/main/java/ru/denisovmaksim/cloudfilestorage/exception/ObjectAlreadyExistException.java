package ru.denisovmaksim.cloudfilestorage.exception;

import lombok.Getter;

@Getter
public class ObjectAlreadyExistException extends RuntimeException {
    private final String path;


    public ObjectAlreadyExistException(String path) {
        this.path = path;
    }

    public ObjectAlreadyExistException(String message, String path) {
        super(message);
        this.path = path;
    }

    public ObjectAlreadyExistException(String message, Throwable cause, String path) {
        super(message, cause);
        this.path = path;
    }

    public ObjectAlreadyExistException(Throwable cause, String path) {
        super(cause);
        this.path = path;
    }

    public ObjectAlreadyExistException(String message, Throwable cause,
                                       boolean enableSuppression, boolean writableStackTrace, String path) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.path = path;
    }

}
