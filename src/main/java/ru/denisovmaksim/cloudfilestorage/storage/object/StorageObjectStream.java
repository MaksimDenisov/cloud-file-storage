package ru.denisovmaksim.cloudfilestorage.storage.object;

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
