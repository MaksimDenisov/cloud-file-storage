package ru.denisovmaksim.cloudfilestorage.service;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.dto.LinkDTO;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.mapper.StorageObjectDTOMapper;
import ru.denisovmaksim.cloudfilestorage.model.User;
import ru.denisovmaksim.cloudfilestorage.repository.FileRepository;
import ru.denisovmaksim.cloudfilestorage.repository.UserRepository;
import ru.denisovmaksim.cloudfilestorage.repository.miniorepository.MinioFileRepository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile({"dev", "prod"})
public class FileService {
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public FileService(MinioFileRepository fileRepository,
                       UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    public List<LinkDTO> getChainLinksFromPath(String path) {
        List<LinkDTO> links = new ArrayList<>();
        String currentItemPath = "";
        for (String dir : path.split("/")) {
            currentItemPath = currentItemPath + dir + "/";
            links.add(new LinkDTO(currentItemPath, dir));
        }
        return links;
    }

    public void createFolder(String path, String folderName) {
        fileRepository.createFolder(getAuthUserId(), path, folderName);
    }

    public List<StorageObjectDTO> getContentOfDirectory(String path) {
        return fileRepository.getStorageObjects(getAuthUserId(), path)
                .stream()
                .map(StorageObjectDTOMapper::toDTO)
                .sorted(Comparator.comparing(StorageObjectDTO::getType).thenComparing(StorageObjectDTO::getName))
                .collect(Collectors.toList());
    }

    public void uploadFile(String path, MultipartFile file) {
        fileRepository.uploadFile(getAuthUserId(), path, file);
    }

    public void renameFolder(String oldFolderName, String newFolderName) {

    }

    public void deleteFolder(String path) {
        fileRepository.deleteFolder(getAuthUserId(), path);
    }

    private Long getAuthUserId() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByName(userName)
                .orElseThrow();
        return user.getId();
    }

    public InputStream downloadFile(String path) {
        return fileRepository.downloadFile(getAuthUserId(), path);
    }
}
