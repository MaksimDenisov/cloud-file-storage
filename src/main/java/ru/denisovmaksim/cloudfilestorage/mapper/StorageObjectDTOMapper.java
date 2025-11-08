package ru.denisovmaksim.cloudfilestorage.mapper;

import ru.denisovmaksim.cloudfilestorage.dto.response.StorageObjectDTOResponse;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;
import ru.denisovmaksim.cloudfilestorage.model.FileType;
import ru.denisovmaksim.cloudfilestorage.util.FileTypeResolver;

public final class StorageObjectDTOMapper {

    private StorageObjectDTOMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static StorageObjectDTOResponse toDTO(StorageObjectInfo object) {
        FileType type = object.isDir()
                ? FileType.FOLDER : FileTypeResolver.detectFileType(object.getPath());
        return new StorageObjectDTOResponse(object.getPath(), object.getName(), type, object.getSize());
    }
}
