package ru.denisovmaksim.cloudfilestorage.service;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.dto.LinkDTO;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.exception.FileStorageException;
import ru.denisovmaksim.cloudfilestorage.mapper.StorageObjectDTOMapper;
import ru.denisovmaksim.cloudfilestorage.storage.MinioFileStorage;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectStream;
import ru.denisovmaksim.cloudfilestorage.storage.StorageObjectsStreams;
import ru.denisovmaksim.cloudfilestorage.model.User;
import ru.denisovmaksim.cloudfilestorage.repository.UserRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
@Profile({"dev", "prod"})
public class FileService {
    private final MinioFileStorage fileRepository;
    private final UserRepository userRepository;

    public FileService(MinioFileStorage fileRepository,
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
        fileRepository.createEmptyFolder(getAuthUserId(), path, folderName);
    }

    public List<StorageObjectDTO> getContentOfDirectory(String path) {
        return fileRepository.getStorageObjects(getAuthUserId(), path)
                .stream()
                .map(StorageObjectDTOMapper::toDTO)
                .sorted(Comparator.comparing(StorageObjectDTO::getType).thenComparing(StorageObjectDTO::getName))
                .collect(Collectors.toList());
    }

    public void uploadFile(String path, MultipartFile file) {
        fileRepository.saveObject(getAuthUserId(), path, file);
    }

    public void renameFolder(String path, String newFolderName) {
        fileRepository.renameFolder(getAuthUserId(), path, newFolderName);
    }

    public void deleteFolder(String path) {
        fileRepository.deleteObjects(getAuthUserId(), path);
    }

    public void deleteFile(String parentPath, String fileName) {
        fileRepository.deleteObjects(getAuthUserId(), parentPath + fileName);
    }

    public NamedStreamDTO getFileAsStream(String path) {
        StorageObjectStream storageObjectStream = fileRepository.getObjectAsStream(getAuthUserId(), path);
        String encodedFileName = URLEncoder.encode(storageObjectStream.getName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new NamedStreamDTO(encodedFileName, storageObjectStream.getStream());
    }

    public NamedStreamDTO getZipFolderAsStream(String path) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StorageObjectsStreams storageObjects = fileRepository.getObjectsAsStreams(getAuthUserId(), path);
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            Map<String, InputStream> map = storageObjects.getStreams();
            for (Map.Entry<String, InputStream> entry : map.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zipOutputStream.putNextEntry(zipEntry);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream objectInputStream = entry.getValue();
                while ((bytesRead = objectInputStream.read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, bytesRead);
                }
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new FileStorageException(e);
        }
        String encodedFileName = URLEncoder.encode(storageObjects.getName() + ".zip", StandardCharsets.UTF_8)
                .replace("+", "%20");
        return new NamedStreamDTO(encodedFileName, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    }

    public void uploadFolder(String path, List<MultipartFile> files) {
        files.forEach(file -> fileRepository.saveObject(getAuthUserId(), path, file));
    }

    private Long getAuthUserId() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByName(userName)
                .orElseThrow();
        return user.getId();
    }
}
