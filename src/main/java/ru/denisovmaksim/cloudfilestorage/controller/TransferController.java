package ru.denisovmaksim.cloudfilestorage.controller;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.service.FileService;

import java.io.InputStream;

import static ru.denisovmaksim.cloudfilestorage.config.ValidationConstants.ERROR_MSG_PATH_INVALID_CHARACTERS;
import static ru.denisovmaksim.cloudfilestorage.config.ValidationConstants.PATH_VALIDATION_REGEXP;

@Controller
@AllArgsConstructor
@Slf4j
@Profile({"dev", "prod"})
@RequestMapping()
public class TransferController {
    private final FileService fileService;

    @GetMapping("/download-folder")
    public ResponseEntity<InputStreamResource> downloadZipFolder(@RequestParam() String path) {
        try {
            log.info("Zip folder from path {}", path);
            NamedStreamDTO dto = fileService.getZipFolderAsStream(path);
            InputStream inputStream = dto.getStream();
            InputStreamResource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", dto.getName()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(-1) // or specify the length if known
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam() String path) {
        try {
            NamedStreamDTO dto = fileService.getFileAsStream(path);
            InputStream inputStream = dto.getStream();
            InputStreamResource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", dto.getName()));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(-1) // or specify the length if known
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload")
    public String uploadFile(
            @Pattern(regexp = PATH_VALIDATION_REGEXP,
                    message = ERROR_MSG_PATH_INVALID_CHARACTERS)
            @ModelAttribute("path") String path,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes attributes) {
        if (!path.isEmpty()) {
            attributes.addAttribute("path", path);
        }
        if (file.isEmpty()) {
            attributes.addFlashAttribute("flashType", "danger");
            attributes.addFlashAttribute("flashMsg", "Please select a file to upload.");
            return "redirect:/";
        }
        log.info("Upload file with name {}", file.getOriginalFilename());
        fileService.uploadFile(path, file);
        return "redirect:/";
    }
}

