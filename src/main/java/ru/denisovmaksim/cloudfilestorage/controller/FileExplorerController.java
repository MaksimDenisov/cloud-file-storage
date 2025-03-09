package ru.denisovmaksim.cloudfilestorage.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.mapper.PathLinksDTOMapper;
import ru.denisovmaksim.cloudfilestorage.service.FileService;
import ru.denisovmaksim.cloudfilestorage.validation.ValidName;
import ru.denisovmaksim.cloudfilestorage.validation.ValidPath;


@Controller
@AllArgsConstructor
@Slf4j
@Validated
public class FileExplorerController {
    private final FileService fileService;

    @PostMapping("/add-folder")
    public String addFolder(@ModelAttribute("folder-name")
                            @ValidName String folderName,
                            @ModelAttribute("path")
                            @ValidPath String path,
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
                             @ValidPath
                             @RequestParam(required = false, defaultValue = "") String path) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("breadcrumbs", PathLinksDTOMapper.toChainLinksFromPath(path));
        model.addAttribute("storageObjects", fileService.getContentOfDirectory(path));
        model.addAttribute("currentPath", path);
        return "explorer/file-explorer";
    }

    @PostMapping("/rename-folder")
    public String renameFolder(@ModelAttribute("redirect-path")
                               @ValidPath String redirectPath,
                               @ModelAttribute("folder-name")
                               @ValidName String folderName,
                               @ModelAttribute("path")
                               @ValidPath String folderPath,
                               RedirectAttributes redirectAttributes) {
        log.info("Rename folder with path {} to {}", folderPath, folderName);
        fileService.renameFolder(folderPath, folderName);
        if (!redirectPath.isEmpty()) {
            redirectAttributes.addAttribute("path", redirectPath);
        }
        return "redirect:/";
    }

    @PostMapping("/delete-folder")
    public String deleteFolder(@ModelAttribute("redirect-path")
                               @ValidPath String redirectPath,
                               @ModelAttribute("folder-path")
                               @ValidPath String folderPath,
                               RedirectAttributes redirectAttributes) {
        log.info("Delete folder with path {}", folderPath);
        fileService.deleteFolder(folderPath);
        if (!redirectPath.isEmpty()) {
            redirectAttributes.addAttribute("path", redirectPath);
        }
        return "redirect:/";
    }

    @PostMapping("/delete-file")
    public String deleteFile(@ModelAttribute("parent-path")
                             @ValidPath String parentPath,
                             @ModelAttribute("file-name")
                             @ValidName String fileName,
                             RedirectAttributes redirectAttributes) {
        log.info("Delete file with path {}", fileName);
        fileService.deleteFile(parentPath, fileName);
        if (!parentPath.isEmpty()) {
            redirectAttributes.addAttribute("path", parentPath);
        }
        return "redirect:/";
    }
}
