package ru.denisovmaksim.cloudfilestorage.storage;

import lombok.Getter;

import java.io.InputStream;
import java.util.Map;

@Getter
public class StorageObjectsStreams extends StorageObject {
    private final Map<String, InputStream> streams;
    StorageObjectsStreams(String path, Map<String, InputStream>  streams) {
        super(path);
        this.streams = streams;
    }
}
