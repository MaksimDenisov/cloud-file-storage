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
        FileType type = object.dir()
                ? FileType.FOLDER : FileTypeResolver.detectFileType(object.path());
        return new StorageObjectDTOResponse(object.path(), object.name(), type, object.size());
    }
}
