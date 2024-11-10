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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ru.denisovmaksim.cloudfilestorage.service.FileService;

import static ru.denisovmaksim.cloudfilestorage.config.ValidationConstants.ERROR_MSG_FILENAME_INVALID;
import static ru.denisovmaksim.cloudfilestorage.config.ValidationConstants.ERROR_MSG_PATH_INVALID_CHARACTERS;
import static ru.denisovmaksim.cloudfilestorage.config.ValidationConstants.FILENAME_VALIDATION_REGEXP;
import static ru.denisovmaksim.cloudfilestorage.config.ValidationConstants.PATH_VALIDATION_REGEXP;


@Controller
@Slf4j
@Profile({"dev", "prod"})
@Validated
public class FileExplorerController {
    private final FileService fileService;

    public FileExplorerController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/add-folder")
    public String addFolder(@ModelAttribute("folder-name")
                            @Pattern(regexp = FILENAME_VALIDATION_REGEXP,
                                    message = ERROR_MSG_FILENAME_INVALID)
                            String folderName,
                            @Pattern(regexp = PATH_VALIDATION_REGEXP,
                                    message = ERROR_MSG_PATH_INVALID_CHARACTERS)
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
                                     message = ERROR_MSG_PATH_INVALID_CHARACTERS)
                             @RequestParam(required = false, defaultValue = "") String path) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("breadcrumbs", fileService.getChainLinksFromPath(path));
        model.addAttribute("storageObjects", fileService.getContentOfDirectory(path));
        model.addAttribute("currentPath", path);
        return "file-explorer";
    }

    @PostMapping("/rename-folder")
    public String renameFolder(@Pattern(regexp = PATH_VALIDATION_REGEXP,
            message = ERROR_MSG_PATH_INVALID_CHARACTERS)
                               @ModelAttribute("redirect-path") String redirectPath,
                               @ModelAttribute("folder-name")
                               @Pattern(regexp = FILENAME_VALIDATION_REGEXP,
                                       message = ERROR_MSG_FILENAME_INVALID)
                               String folderName,
                               @Pattern(regexp = PATH_VALIDATION_REGEXP,
                                       message = ERROR_MSG_PATH_INVALID_CHARACTERS)
                               @ModelAttribute("path") String folderPath,
                               RedirectAttributes redirectAttributes) {
        log.info("Rename folder with path {} to {}", folderPath, folderName);
        fileService.renameFolder(folderPath, folderName);
        if (!redirectPath.isEmpty()) {
            redirectAttributes.addAttribute("path", redirectPath);
        }
        return "redirect:/";
    }

    @PostMapping("/delete-folder")
    public String deleteFolder(@Pattern(regexp = PATH_VALIDATION_REGEXP,
            message = ERROR_MSG_PATH_INVALID_CHARACTERS)
                               @ModelAttribute("redirect-path") String redirectPath,
                               @Pattern(regexp = PATH_VALIDATION_REGEXP,
                                       message = ERROR_MSG_PATH_INVALID_CHARACTERS)
                               @ModelAttribute("folder-path") String folderPath,
                               RedirectAttributes redirectAttributes) {
        log.info("Delete folder with path {}", folderPath);
        fileService.deleteFolder(folderPath);
        if (!redirectPath.isEmpty()) {
            redirectAttributes.addAttribute("path", redirectPath);
        }
        return "redirect:/";
    }

    @PostMapping("/delete-file")
    public String deleteFile(@Pattern(regexp = PATH_VALIDATION_REGEXP,
            message = ERROR_MSG_PATH_INVALID_CHARACTERS)
                             @ModelAttribute("parent-path") String parentPath,
                             @Pattern(regexp = FILENAME_VALIDATION_REGEXP,
                                     message = ERROR_MSG_FILENAME_INVALID)
                             @ModelAttribute("file-name") String fileName,
                             RedirectAttributes redirectAttributes) {
        log.info("Delete file with path {}", fileName);
        fileService.deleteFile(parentPath, fileName);
        if (!parentPath.isEmpty()) {
            redirectAttributes.addAttribute("path", parentPath);
        }
        return "redirect:/";
    }
}
