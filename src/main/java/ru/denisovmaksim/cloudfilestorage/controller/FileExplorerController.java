package ru.denisovmaksim.cloudfilestorage.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.dto.DirectoryDTO;
import ru.denisovmaksim.cloudfilestorage.service.FileService;


@Controller
@Slf4j
@Profile({"dev", "prod"})
public class FileExplorerController {

    private final FileService fileService;

    public FileExplorerController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/add-folder")
    public String addFolder(@ModelAttribute("folder-name") String folderName,
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
    public String getFiles(Model model, Authentication authentication,
                           @RequestParam(required = false, defaultValue = "") String path) {
        DirectoryDTO content = fileService.getContentOfDirectory(path);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("content", content);
        return "file-explorer";
    }

    @PostMapping("/delete-folder")
    public String deleteFolder(@ModelAttribute("name") String folderName,
                               @ModelAttribute("path") String path,
                               RedirectAttributes redirectAttributes) {
        log.info("Delete folder with name {}", folderName);
        fileService.deleteFolder(path + folderName);
        if (!path.isEmpty()) {
            redirectAttributes.addAttribute("path", path);
        }
        return "redirect:/";
    }
}
