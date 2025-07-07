package ru.denisovmaksim.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import ru.denisovmaksim.cloudfilestorage.validation.PathType;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;


@AllArgsConstructor
@Getter
public final class RequestUploadFileDTO {
    @ValidPath(PathType.FILENAME)
    private String filename;
    private MultipartFile multipartFile;
}
