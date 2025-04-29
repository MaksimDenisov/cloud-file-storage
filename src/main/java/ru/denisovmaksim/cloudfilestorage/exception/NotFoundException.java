package ru.denisovmaksim.cloudfilestorage.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    private final String path;

    public NotFoundException(String path) {
        super(path + " was not found");
        this.path = path;
    }
}
