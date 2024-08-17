package ru.denisovmaksim.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class FolderDTO {
    private final String path;
    private final String name;
    private final Long size;
}
