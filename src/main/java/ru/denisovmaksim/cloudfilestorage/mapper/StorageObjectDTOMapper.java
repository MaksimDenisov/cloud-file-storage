package ru.denisovmaksim.cloudfilestorage.mapper;

import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.storage.object.StorageObject;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectType;

public class StorageObjectDTOMapper {
    public static StorageObjectDTO toDTO(StorageObject object) {
        StorageObjectType type = object.isFolder()
                ? StorageObjectType.FOLDER : StorageObjectType.UNKNOWN_FILE;
        return new StorageObjectDTO(object.getPath(), object.getName(), type, object.getSize());
    }
}
