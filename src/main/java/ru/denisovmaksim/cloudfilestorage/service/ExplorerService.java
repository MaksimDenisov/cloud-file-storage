package ru.denisovmaksim.cloudfilestorage.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.denisovmaksim.cloudfilestorage.dto.response.StorageObjectDTOResponse;
import ru.denisovmaksim.cloudfilestorage.exception.NotFoundException;
import ru.denisovmaksim.cloudfilestorage.mapper.StorageObjectDTOMapper;
import ru.denisovmaksim.cloudfilestorage.storage.MinioMetadataAccessor;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectInfo;
import ru.denisovmaksim.cloudfilestorage.validation.PathType;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@AllArgsConstructor
public class ExplorerService {
    private final MinioMetadataAccessor minioMetadataAccessor;



    private final SecurityService securityService;

    public List<StorageObjectDTOResponse> getFolder(@ValidPath(PathType.DIR) String directory) {
        Long authUserId = securityService.getAuthUserId();
        List<StorageObjectInfo> infos = minioMetadataAccessor.listObjectInfo(authUserId, directory)
                .orElseThrow(() -> new NotFoundException(directory));
        for (StorageObjectInfo info : infos) {
            if (info.isDir()) {
                info.setSize(minioMetadataAccessor.getDirectChildCount(authUserId, info.getPath()));
            }
        }
        return infos.stream()
                .map(StorageObjectDTOMapper::toDTO)
                .sorted(Comparator.comparing(StorageObjectDTOResponse::getType)
                        .thenComparing(StorageObjectDTOResponse::getName))
                .collect(Collectors.toList());
    }


}
