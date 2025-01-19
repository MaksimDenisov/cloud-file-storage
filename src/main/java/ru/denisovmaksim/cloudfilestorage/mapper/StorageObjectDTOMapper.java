package ru.denisovmaksim.cloudfilestorage.mapper;

import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectType;

public class StorageObjectDTOMapper {
    public static StorageObjectDTO toDTO(StorageObject object) {
        String path = object.getPath();
        String[] element = object.getPath().split("/");
        String name = element[element.length - 1];
        if (name.endsWith("/")) {
            name = name.replaceFirst(".$", "");
        }
        StorageObjectType type = (path.endsWith("/"))
                ? StorageObjectType.FOLDER : StorageObjectType.UNKNOWN_FILE;
        Long size = object.getSize();
        return new StorageObjectDTO(path, name, type, size);
    }
}
