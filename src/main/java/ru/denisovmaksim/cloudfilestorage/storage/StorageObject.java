package ru.denisovmaksim.cloudfilestorage.storage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;


@Getter
@ToString
@Slf4j
public class StorageObject {
    private String parentPath = "";

    private final String name;

    private final boolean folder;

    @Setter
    private long size;

    StorageObject(String path) {
        this.folder = path.endsWith("/");
        if (path.endsWith("/")) {
            this.name = path.substring(path.lastIndexOf('/', path.length() - 2) + 1, path.length() - 1);
            path = path.substring(0, path.length() - 1);
        } else {
            this.name = path.substring(path.lastIndexOf('/') + 1);
        }
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            this.parentPath = path.substring(0, lastSlashIndex + 1);
        }
    }

    public String getPath() {
        return parentPath + name + (isFolder() ? "/" : "");
    }
}
