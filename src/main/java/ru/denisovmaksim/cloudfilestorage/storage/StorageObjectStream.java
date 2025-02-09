package ru.denisovmaksim.cloudfilestorage.storage;

import lombok.Getter;

import java.io.InputStream;

@Getter
public class StorageObjectStream extends StorageObject {
    private final InputStream stream;

    StorageObjectStream(String path, InputStream stream) {
        super(path);
        this.stream = stream;
    }

}
