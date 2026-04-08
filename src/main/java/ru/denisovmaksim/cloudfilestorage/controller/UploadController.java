package ru.denisovmaksim.cloudfilestorage.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.model.DirPath;
import ru.denisovmaksim.cloudfilestorage.service.UploadService;

import java.util.List;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping()
public class UploadController {
    private static final String REDIRECT_TO_ROOT = "redirect:/";
    private UploadService uploadService;

    @PostMapping("/upload-file")
    public String uploadFile(@ModelAttribute("path") String parentPath,
                             @RequestParam("file") MultipartFile multipartFile,
                             RedirectAttributes attributes) {
        if (!parentPath.isEmpty()) {
            attributes.addAttribute("path", parentPath);
        }
        if (multipartFile.isEmpty()) {
            attributes.addFlashAttribute("flashType", "danger");
            attributes.addFlashAttribute("flashMsg", "Please select a file to upload.");
            return REDIRECT_TO_ROOT;
        }
        log.info("Upload file with name {}", multipartFile.getOriginalFilename());
        String filename = (multipartFile.getOriginalFilename() == null) ? "file" : multipartFile.getOriginalFilename();
        uploadService.uploadFile(DirPath.of(parentPath), filename, multipartFile);
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
        uploadService.uploadFolder(DirPath.of(path), files);
        return REDIRECT_TO_ROOT;
    }
}
