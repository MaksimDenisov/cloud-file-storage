package ru.denisovmaksim.cloudfilestorage.mapper;

import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.dto.DirectoryDTO;
import ru.denisovmaksim.cloudfilestorage.dto.FolderDTO;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;
import ru.denisovmaksim.cloudfilestorage.model.StorageObjectType;

import java.util.List;

@Component
public class StorageObjectsToDirectoryDTOMapper {

    public DirectoryDTO toDto(String workingDirectory, List<StorageObject> objects) {
        DirectoryDTO dto = new DirectoryDTO();
        dto.setPath(workingDirectory);
        String path = "";
        for (String dir : workingDirectory.split("/")) {
            path = path + dir + "/";
            //TODO add breadcrumbs dto
            dto.getUpperDirs().add(new FolderDTO(path, dir, 0L));
        }
        for (StorageObject object : objects) {
            if (object.getType().equals(StorageObjectType.FOLDER)) {
                dto.getFolders().add(new FolderDTO(object.getPath(), object.getName(), object.getSize()));
            }
        }
        return dto;
    }
}
