package ru.denisovmaksim.cloudfilestorage.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@RequiredArgsConstructor
@Getter
@ToString
public class StorageObject {

    private final String path;

    @Setter
    private long size;

    public boolean isFolder() {
        return path.endsWith("/");
    }
}
