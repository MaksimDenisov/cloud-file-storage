package ru.denisovmaksim.cloudfilestorage.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.denisovmaksim.cloudfilestorage.dto.NamedStreamDTO;
import ru.denisovmaksim.cloudfilestorage.model.FileType;
import ru.denisovmaksim.cloudfilestorage.service.PreviewService;
import ru.denisovmaksim.cloudfilestorage.util.FileTypeResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping("/preview")
public class PreviewController {
    private final PreviewService previewService;

    @GetMapping("")
    public String getPreviewPage(Model model, @RequestParam() String filepath) {
        model.addAttribute("filepath", filepath);
        FileType type = FileTypeResolver.detectFileType(filepath);
        return switch (type) {
            case FOLDER -> "redirect:/";
            case UNKNOWN_FILE -> "preview/unknown";
            case IMAGE -> "preview/image";
            case MUSIC -> "preview/music";
            case TEXT -> "preview/text";
        };
    }
    @GetMapping("/music")
    public ResponseEntity<Resource> getPreviewMusic(@RequestParam() String filepath) {
        NamedStreamDTO dto = previewService.getMusic(filepath);
        InputStream stream = dto.getStream();

        String contentType = detectMimeType(filepath);
        long contentLength = dto.getLength();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + dto.getName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(contentLength)
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/image")
    public ResponseEntity<Resource> getPreviewImage(@RequestParam() String filepath) {
        NamedStreamDTO dto = previewService.getImage(filepath);
        InputStream stream = dto.getStream();

        String contentType = detectMimeType(filepath);
        long contentLength = dto.getLength();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + dto.getName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(contentLength)
                .body(new InputStreamResource(stream));
    }

    private String detectMimeType(String path) {
        try {
            return Files.probeContentType(Path.of(path));
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}
