package ru.denisovmaksim.cloudfilestorage.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@AllArgsConstructor
public final class StorageObjectInfo {

    private final String path;

    private final String name;

    private final boolean dir;

    @Setter
    private long size;
}
