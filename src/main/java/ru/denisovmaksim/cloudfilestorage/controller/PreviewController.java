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
import ru.denisovmaksim.cloudfilestorage.util.FilesUtil;

import java.io.InputStream;

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
            case IMAGE -> "preview/image";
            case AUDIO -> "preview/audio";
            default -> "redirect:/";
        };
    }

    @GetMapping("/image")
    public ResponseEntity<Resource> getPreviewImage(@RequestParam() String filepath) {
        NamedStreamDTO dto = previewService.getImage(filepath);
        InputStream stream = dto.getStream();

        String contentType =  FilesUtil.detectMimeType(filepath);
        long contentLength = dto.getLength();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + dto.getName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(contentLength)
                .body(new InputStreamResource(stream));
    }
}
