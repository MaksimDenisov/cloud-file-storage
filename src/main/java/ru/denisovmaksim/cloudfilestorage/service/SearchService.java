package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.denisovmaksim.cloudfilestorage.dto.FileType;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class SearchService {

    private final MinioFileStorage fileStorage;

    private final SecurityService securityService;

    public List<StorageObjectDTO> search(String query) {
        Long userId = securityService.getAuthUserId();
        List<StorageObjectInfo> objectInfos = fileStorage.searchObjectInfo(userId, "", query);
        objectInfos.forEach(a -> log.info("File {} at path {}", a.getPath(), a.getPath()));
        Map<String, StorageObjectDTO> dtoMap = new HashMap<>();
        for (StorageObjectInfo info : objectInfos) {
            int index = info.getPath().toUpperCase().indexOf(query.toUpperCase());
            while (index != -1) {
                int partPathEnd = info.getPath().indexOf("/", index);
                partPathEnd = (partPathEnd != -1) ? partPathEnd + 1 : info.getPath().length();
                String path = info.getPath().substring(0, partPathEnd);
                log.info("Find path {}", path);
                if (!dtoMap.containsKey(path)) {
                    FileType type = (info.isFolder())
                            ? FileType.FOLDER : FileType.UNKNOWN_FILE;

                    //TODO add calculate size option probably extract another method
                    if (info.isFolder()) {
                        info.setSize(fileStorage.getDirectChildCount(userId, info.getPath()));
                    }

                    StorageObjectDTO dto = new StorageObjectDTO(PathUtil.getParentDirName(path),
                            path.replace(PathUtil.getParentDirName(path), ""),
                            type, info.getSize());
                    dtoMap.put(path, dto);
                }
                index = info.getPath().toUpperCase().indexOf(query.toUpperCase(), index + 1);
            }
        }

        return dtoMap.values().stream()
                .sorted(Comparator.comparing(StorageObjectDTO::getType).thenComparing(StorageObjectDTO::getName))
                .collect(Collectors.toList());
    }
}
