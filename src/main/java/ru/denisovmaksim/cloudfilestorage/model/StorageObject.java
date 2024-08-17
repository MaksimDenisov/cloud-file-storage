package ru.denisovmaksim.cloudfilestorage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;


@AllArgsConstructor
@Getter
public class StorageObject {

    private String path;

    private String name;

    private StorageObjectType type;

    @Setter
    private Long size;

    private ZonedDateTime lastModified;

}
