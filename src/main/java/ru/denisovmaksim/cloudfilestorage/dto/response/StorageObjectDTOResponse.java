package ru.denisovmaksim.cloudfilestorage.dto.response;

import ru.denisovmaksim.cloudfilestorage.model.FileType;

public record StorageObjectDTOResponse(String fullPath, String name, FileType type, Long size) {
}
