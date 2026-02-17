package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.dto.response.StorageObjectDTOResponse;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.mapper.StorageObjectDTOMapper;
import ru.denisovmaksim.cloudfilestorage.storage.StorageMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;
import ru.denisovmaksim.cloudfilestorage.validation.PathType;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@AllArgsConstructor
public class ExplorerService {
    private final StorageMetadataAccessor metadataAccessor;
    private final SecurityService securityService;

    public List<StorageObjectDTOResponse> getFolder(@ValidPath(PathType.DIR) String directory) {
        Long authUserId = securityService.getAuthUserId();
        List<StorageObjectInfo> infos = metadataAccessor.listObjectInfo(authUserId, directory)
                .orElseThrow(() -> new NotFoundException(directory));
        List<StorageObjectInfo> infosWithDirSize = new ArrayList<>();
        for (StorageObjectInfo info : infos) {
            long size;
            if (info.dir()) {
                size = metadataAccessor
                        .listObjectInfo(authUserId, info.path()).orElse(Collections.emptyList()).size();
            } else {
                size = info.size();
            }
            infosWithDirSize.add(new StorageObjectInfo(info.path(), info.name(), info.dir(), size));
        }
        return infosWithDirSize.stream()
                .map(StorageObjectDTOMapper::toDTO)
                .sorted(Comparator.comparing(StorageObjectDTOResponse::type)
                        .thenComparing(StorageObjectDTOResponse::name))
                .collect(Collectors.toList());
    }
}
