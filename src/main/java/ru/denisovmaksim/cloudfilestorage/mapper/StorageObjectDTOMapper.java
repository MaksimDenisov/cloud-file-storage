package ru.denisovmaksim.cloudfilestorage.mapper;

import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;
import ru.denisovmaksim.cloudfilestorage.dto.FileType;

public class StorageObjectDTOMapper {
    public static StorageObjectDTO toDTO(StorageObjectInfo object) {
        FileType type = object.isFolder()
                ? FileType.FOLDER : FileType.UNKNOWN_FILE;
        return new StorageObjectDTO(object.getParentPath(), object.getName(), type, object.getSize());
    }
}
