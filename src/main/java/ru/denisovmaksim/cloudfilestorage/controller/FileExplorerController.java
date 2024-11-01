package ru.denisovmaksim.cloudfilestorage.controller;

import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.service.FileService;


@Controller
@Slf4j
@Profile({"dev", "prod"})
@Validated
public class FileExplorerController {
    public static final String FILENAME_VALIDATION_REGEXP = "^[^/\\\\:*?\"<>|]+$";
    public static final String FILENAME_INVALID_MESSAGE = "Filename must not contains / \\ : * ? \\ \" < > | ";
    public static final String PATH_VALIDATION_REGEXP = "^([^\\\\:*?\"<>|]*\\/)?$";
    public static final String PATH_INVALID_CHARACTERS = "Not valid path";

    private final FileService fileService;

    public FileExplorerController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/add-folder")
    public String addFolder(@ModelAttribute("folder-name")
                            @Pattern(regexp = FILENAME_VALIDATION_REGEXP,
                                    message = FILENAME_INVALID_MESSAGE)
                            String folderName,
                            @Pattern(regexp = PATH_VALIDATION_REGEXP,
                                    message = PATH_INVALID_CHARACTERS)
                            @ModelAttribute("path") String path,
                            RedirectAttributes redirectAttributes) {
        log.info("Add folder with name {}", folderName);
        fileService.createFolder(path, folderName);
        if (!path.isEmpty()) {
            redirectAttributes.addAttribute("path", path);
        }
        return "redirect:/";
    }

    @GetMapping("/")
    public String getObjects(Model model, Authentication authentication,
                             @Pattern(regexp = PATH_VALIDATION_REGEXP,
                                     message = PATH_INVALID_CHARACTERS)
                             @RequestParam(required = false, defaultValue = "") String path) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("breadcrumbs", fileService.getChainLinksFromPath(path));
        model.addAttribute("storageObjects", fileService.getContentOfDirectory(path));
        model.addAttribute("currentPath", path);
        return "file-explorer";
    }

    @PostMapping("/delete-folder")
    public String deleteFolder(
            @Pattern(regexp = PATH_VALIDATION_REGEXP,
                    message = PATH_INVALID_CHARACTERS)
            @ModelAttribute("redirect-path") String redirectPath,
            @Pattern(regexp = PATH_VALIDATION_REGEXP,
                    message = PATH_INVALID_CHARACTERS)
            @ModelAttribute("folder-path") String folderPath,
            RedirectAttributes redirectAttributes) {
        log.info("Delete folder with path {}", folderPath);
        fileService.deleteFolder(folderPath);
        if (!redirectPath.isEmpty()) {
            redirectAttributes.addAttribute("path", redirectPath);
        }
        return "redirect:/";
    }

    @PostMapping("/upload")
    public String uploadFile(
            @Pattern(regexp = PATH_VALIDATION_REGEXP,
                    message = PATH_INVALID_CHARACTERS)
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
