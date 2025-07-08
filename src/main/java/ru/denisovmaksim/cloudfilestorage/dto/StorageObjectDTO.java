package ru.denisovmaksim.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StorageObjectDTO {

    private final String parentDir;

    private final String name;

    private final FileType type;

    private final Long size;
}
