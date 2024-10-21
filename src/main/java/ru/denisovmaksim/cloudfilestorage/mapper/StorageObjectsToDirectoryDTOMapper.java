package ru.denisovmaksim.cloudfilestorage.mapper;

import org.springframework.stereotype.Component;
import ru.denisovmaksim.cloudfilestorage.dto.DirectoryDTO;
import ru.denisovmaksim.cloudfilestorage.dto.FolderDTO;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StorageObjectsToDirectoryDTOMapper {

    public DirectoryDTO toDto(String workingDirectory, List<StorageObject> objects) {
        List<FolderDTO> breadcrumbs = new ArrayList<>();
        String path = "";
        for (String dir : workingDirectory.split("/")) {
            path = path + dir + "/";
            //TODO add breadcrumbs dto
            breadcrumbs.add(new FolderDTO(path, dir, 0L));
        }
        List<StorageObject> sortedObjects = objects.stream()
                .sorted(Comparator.comparing(StorageObject::getType).thenComparing(StorageObject::getName))
                .collect(Collectors.toList());
        return new DirectoryDTO(workingDirectory, breadcrumbs, sortedObjects);
    }
}
