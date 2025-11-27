package ru.denisovmaksim.cloudfilestorage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import ru.denisovmaksim.cloudfilestorage.model.FileType;

@AllArgsConstructor
@Getter
@ToString
public class StorageObjectDTOResponse {

    private final String fullPath;

    private final String name;

    private final FileType type;

    private final Long size;
}
