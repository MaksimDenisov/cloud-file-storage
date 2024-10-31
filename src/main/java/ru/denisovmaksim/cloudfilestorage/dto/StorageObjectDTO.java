package ru.denisovmaksim.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.denisovmaksim.cloudfilestorage.model.StorageObjectType;

@AllArgsConstructor
@Getter
public class StorageObjectDTO {
    private final String path;

    private final String name;

    private final StorageObjectType type;

    private final Long size;
}
