package ru.denisovmaksim.cloudfilestorage.dto;

import lombok.Getter;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;

import java.util.List;

@Getter
public class DirectoryDTO {
    private final String path;
    private final List<FolderDTO> upperDirs;
    //private final List<FolderDTO> folders = new ArrayList<>();
    private final List<StorageObject> objects;


    public DirectoryDTO(String path, List<FolderDTO> upperDirs, List<StorageObject> objects) {
        this.path = path;
        this.upperDirs = upperDirs;
        this.objects = objects;
    }
}
