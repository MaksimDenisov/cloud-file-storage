package ru.denisovmaksim.cloudfilestorage.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.denisovmaksim.cloudfilestorage.service.FileService;

import java.io.InputStream;

@Controller
@AllArgsConstructor
@Slf4j
@Profile({"dev", "prod"})
@RequestMapping("/download")
public class DownloadController {
    private final FileService fileService;

    @GetMapping("")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam() String path) {
        try {
            InputStream inputStream = fileService.downloadFile(path);
            InputStreamResource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "file");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(-1) // or specify the length if known
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

