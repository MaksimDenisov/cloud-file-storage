package ru.denisovmaksim.cloudfilestorage.service;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.denisovmaksim.cloudfilestorage.dto.DirectoryDTO;
import ru.denisovmaksim.cloudfilestorage.mapper.StorageObjectsToDirectoryDTOMapper;
import ru.denisovmaksim.cloudfilestorage.model.StorageObject;
import ru.denisovmaksim.cloudfilestorage.model.User;
import ru.denisovmaksim.cloudfilestorage.repository.FileRepository;
import ru.denisovmaksim.cloudfilestorage.repository.miniorepository.MinioFileRepository;
import ru.denisovmaksim.cloudfilestorage.repository.UserRepository;

import java.util.List;

@Service
@Profile({"dev", "prod"})
public class FileService {
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final StorageObjectsToDirectoryDTOMapper mapper;

    public FileService(MinioFileRepository fileRepository,
                       UserRepository userRepository,
                       StorageObjectsToDirectoryDTOMapper mapper) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public void createFolder(String path, String folderName) {
        fileRepository.createFolder(getAuthUserId(), path, folderName);
    }

    public DirectoryDTO getContentOfDirectory(String path) {
        List<StorageObject> objects = fileRepository.getStorageObjects(getAuthUserId(), path);
        return mapper.toDto(path, objects);
    }

    public void renameFolder(String oldFolderName, String newFolderName) {

    }

    public void deleteFolder(String folderName) {

    }

    private Long getAuthUserId() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByName(userName)
                .orElseThrow();
        return user.getId();
    }
}
