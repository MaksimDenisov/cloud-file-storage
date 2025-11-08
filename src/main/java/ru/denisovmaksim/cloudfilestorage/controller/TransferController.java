package ru.denisovmaksim.cloudfilestorage.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.denisovmaksim.cloudfilestorage.dto.response.NamedStreamDTOResponse;
import ru.denisovmaksim.cloudfilestorage.dto.request.UploadFileDTORequest;
import ru.denisovmaksim.cloudfilestorage.service.TransferService;

import java.io.InputStream;
import java.util.List;


@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping()
public class TransferController {

    private static final String REDIRECT_TO_ROOT = "redirect:/";

    private final TransferService transferService;

    @GetMapping("/download-folder")
    public ResponseEntity<InputStreamResource> downloadZipFolder(@RequestParam() String path) {
        try {
            log.info("Download zip folder from path {}", path);
            return createStreamResponse(transferService.getZipFolderAsStream(path));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam() String path) {
        try {
            return createStreamResponse(transferService.getFileAsStream(path));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload-file")
    public String uploadFile(@ModelAttribute("path") String path,
                             @RequestParam("file") MultipartFile multipartFile,
                             RedirectAttributes attributes) {
        if (!path.isEmpty()) {
            attributes.addAttribute("path", path);
        }
        if (multipartFile.isEmpty()) {
            attributes.addFlashAttribute("flashType", "danger");
            attributes.addFlashAttribute("flashMsg", "Please select a file to upload.");
            return REDIRECT_TO_ROOT;
        }
        log.info("Upload file with name {}", multipartFile.getOriginalFilename());
        String filename = (multipartFile.getOriginalFilename() == null) ? "file" : multipartFile.getOriginalFilename();
        UploadFileDTORequest fileDTO = new UploadFileDTORequest(filename, multipartFile);
        transferService.uploadFile(path, fileDTO);
        return REDIRECT_TO_ROOT;
    }

    @PostMapping("/upload-folder")
    public String uploadFolder(@ModelAttribute("path") String path,
                               @RequestParam("files") List<MultipartFile> files,
                               RedirectAttributes attributes) {
        if (!path.isEmpty()) {
            attributes.addAttribute("path", path);
        }
        log.info("Folder" + path);
        for (MultipartFile file : files) {
            log.info("Upload file: " + file.getOriginalFilename());
        }
        transferService.uploadFolder(path, files);
        return REDIRECT_TO_ROOT;
    }

    private ResponseEntity<InputStreamResource> createStreamResponse(NamedStreamDTOResponse dto) {
        InputStream inputStream = dto.getStream();
        InputStreamResource resource = new InputStreamResource(inputStream);
        long contentLength = dto.getLength();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", dto.getName()));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(contentLength)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}

