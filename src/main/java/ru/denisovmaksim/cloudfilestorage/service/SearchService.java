package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.denisovmaksim.cloudfilestorage.mapper.StorageObjectDTOMapper;
import ru.denisovmaksim.cloudfilestorage.dto.response.StorageObjectDTOResponse;
import ru.denisovmaksim.cloudfilestorage.model.FileType;
import ru.denisovmaksim.cloudfilestorage.storage.MinioMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@Slf4j
public class SearchService {

    private final MinioMetadataAccessor metadataAccessor;

    private final SecurityService securityService;

    public List<StorageObjectDTOResponse> search(String query) {
        Long userId = securityService.getAuthUserId();
        List<StorageObjectInfo> objectInfos = metadataAccessor
                .findObjectInfosBySubstring(userId, "", query).stream().toList();
        objectInfos.forEach(a -> log.info("Found {}", a.getPath()));
        Set<String> paths = getPathsContainsSubstring(objectInfos, query);
        for (StorageObjectInfo info : objectInfos) {
            paths.remove(info.getPath());
        }
        List<StorageObjectInfo> phantomFolders = new ArrayList<>();
        for (String path : paths) {
            log.info("Found folder {}", path);
            String name = PathUtil.getBaseName(path);
            boolean isDir = PathUtil.isDir(path);
            Long size = metadataAccessor.getDirectChildCount(securityService.getAuthUserId(), path);
            StorageObjectInfo storageObjectInfo = new StorageObjectInfo(path, name, isDir, size);
            phantomFolders.add(storageObjectInfo);
        }
        return Stream.concat(objectInfos.stream(), phantomFolders.stream())
                .map(StorageObjectDTOMapper::toDTO)
                .filter(dto -> dto.getName().contains(query))
                .sorted(Comparator.comparing((StorageObjectDTOResponse dto) -> dto.getType() == FileType.FOLDER ? 0 : 1)
                        .thenComparing(StorageObjectDTOResponse::getName))
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
