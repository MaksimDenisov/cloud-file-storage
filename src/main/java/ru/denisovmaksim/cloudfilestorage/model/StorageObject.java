package ru.denisovmaksim.cloudfilestorage.model;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;


@Getter
public class StorageObject {

    private final String path;

    private final String name;

    private final StorageObjectType type;

    @Setter
    private Long size;

    private final ZonedDateTime lastModified;

    public StorageObject(String path, String name, StorageObjectType type, Long size, ZonedDateTime lastModified) {
        this(path, name, type, lastModified);
        this.size = size;
    }

    public StorageObject(String path, String name, StorageObjectType type, ZonedDateTime lastModified) {
        this.path = path;
        this.name = name;
        this.type = type;
        this.lastModified = lastModified;
    }

    public String getName() {
        return name + ((type == StorageObjectType.FOLDER) ? "/" : "");
    }
}
