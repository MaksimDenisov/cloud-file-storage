package ru.denisovmaksim.cloudfilestorage.mapper;

import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;
import ru.denisovmaksim.cloudfilestorage.dto.FileType;

public final class StorageObjectDTOMapper {

    private StorageObjectDTOMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static StorageObjectDTO toDTO(StorageObjectInfo object) {
        FileType type = object.isDir()
                ? FileType.FOLDER : FileType.UNKNOWN_FILE;
        return new StorageObjectDTO(object.getPath(), object.getName(), type, object.getSize());
    }
}
