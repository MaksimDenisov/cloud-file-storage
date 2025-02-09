package ru.denisovmaksim.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StorageObjectDTO {
    private final String parentPath;

    private final String name;

    private final FileType type;

    private final Long size;

    public String getPath() {
        return parentPath + name + (type.equals(FileType.FOLDER)  ? "/" : "");
    }
}
