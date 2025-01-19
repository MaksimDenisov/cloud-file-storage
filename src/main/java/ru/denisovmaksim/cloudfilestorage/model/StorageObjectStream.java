package ru.denisovmaksim.cloudfilestorage.model;

import lombok.Getter;

import java.io.InputStream;

@Getter
public class StorageObjectStream extends StorageObject {
    private final InputStream stream;

    public StorageObjectStream(String path, InputStream stream) {
        super(path);
        this.stream = stream;
    }

}
