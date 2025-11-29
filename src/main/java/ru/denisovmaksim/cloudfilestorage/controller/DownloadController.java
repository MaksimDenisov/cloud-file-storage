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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.denisovmaksim.cloudfilestorage.dto.response.NamedStreamDTOResponse;
import ru.denisovmaksim.cloudfilestorage.service.DownloadService;

import java.io.InputStream;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping()
public class DownloadController {

    private final DownloadService downloadService;

    @GetMapping("/download-folder")
    public ResponseEntity<InputStreamResource> downloadZipFolder(@RequestParam() String path) {
        try {
            log.info("Download zip folder from path {}", path);
            return createStreamResponse(downloadService.getZipFolderAsStream(path));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam() String path) {
        try {
            return createStreamResponse(downloadService.getFileAsStream(path));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

