package ru.denisovmaksim.cloudfilestorage.model;

import lombok.Getter;

import java.io.InputStream;
import java.util.Map;

@Getter
public class StorageObjectsStreams extends StorageObject {
    private final Map<String, InputStream> streams;
    public StorageObjectsStreams(String path, Map<String, InputStream>  streams) {
        super(path);
        this.streams = streams;
    }
}
