package ru.denisovmaksim.cloudfilestorage.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;


@Getter
@Builder
public class StorageObject {

    private final String path;

    private final String name;

    private final StorageObjectType type;

    @Setter
    private Long size;

    private final ZonedDateTime lastModified;

}
