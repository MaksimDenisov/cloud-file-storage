package ru.denisovmaksim.cloudfilestorage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DirectoryDTO {
    private String path = "";
    private final List<FolderDTO> upperDirs = new ArrayList<>();
    private final List<FolderDTO> folders = new ArrayList<>();
    private final List<StorageObject> files = new ArrayList<>();
}
