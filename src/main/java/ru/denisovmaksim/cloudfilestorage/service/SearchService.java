package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.denisovmaksim.cloudfilestorage.model.FileType;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.storage.MinioMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class SearchService {

    private final MinioMetadataAccessor fileStorage;

    private final SecurityService securityService;

    public List<StorageObjectDTO> search(String query) {
        Long userId = securityService.getAuthUserId();
        List<StorageObjectInfo> objectInfos = fileStorage.searchObjectInfo(userId, "", query).stream().toList();
        objectInfos.forEach(a -> log.info("File {} at path {}", a.getName(), a.getPath()));
        Set<String> paths = getPathsContainsSubstring(objectInfos, query);
        paths.forEach(path -> log.info("Find path {}", path));
        Map<String, StorageObjectDTO> dtoMap = new HashMap<>();
        for (String path : paths) {
            StorageObjectDTO dto;
            if (PathUtil.isDir(path)) {
                dto = new StorageObjectDTO(path,
                        path.replace(PathUtil.getParentPath(path), ""),
                        FileType.FOLDER, fileStorage.getDirectChildCount(userId, path));
            } else {
                FileType type = FileType.UNKNOWN_FILE;
                StorageObjectInfo storageObjectInfo = objectInfos.stream()
                        .filter(info -> info.getPath().equals(path))
                        .findAny()
                        .orElseThrow();
                dto = new StorageObjectDTO(path,
                        path.replace(PathUtil.getParentPath(path), ""),
                        type, storageObjectInfo.getSize());

            }
            dtoMap.put(path, dto);
        }
        return dtoMap.values().stream()
                .sorted(Comparator.comparing(StorageObjectDTO::getType).thenComparing(StorageObjectDTO::getName))
                .collect(Collectors.toList());
    }

    private Set<String> getPathsContainsSubstring(List<StorageObjectInfo> objectInfos, String substring) {
        Set<String> paths = new HashSet<>();
        for (StorageObjectInfo info : objectInfos) {
            int index = info.getPath().toUpperCase().indexOf(substring.toUpperCase());
            while (index != -1) {
                int partPathEnd = info.getPath().indexOf("/", index);
                partPathEnd = (partPathEnd != -1) ? partPathEnd + 1 : info.getPath().length();
                String path = info.getPath().substring(0, partPathEnd);
                paths.add(path);
                index = info.getPath().toUpperCase().indexOf(substring.toUpperCase(), index + 1);
            }
        }
        return paths;
    }
}
