package ru.denisovmaksim.cloudfilestorage.storage;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@ToString
@EqualsAndHashCode
public final class StorageObjectInfo {

    private final String path;

    private final String name;

    private final boolean dir;

    @Setter
    private long size;

    StorageObjectInfo(String path, String name, boolean isDir, long size) {
        this.path = path;
        this.name = name;
        this.dir = isDir;
        this.size = size;
    }
}
