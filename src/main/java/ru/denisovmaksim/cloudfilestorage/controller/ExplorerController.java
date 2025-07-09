package ru.denisovmaksim.cloudfilestorage.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.mapper.PathLinksDTOMapper;
import ru.denisovmaksim.cloudfilestorage.service.ExplorerService;


@Controller
@AllArgsConstructor
@Slf4j
public class ExplorerController {

    private static final String REDIRECT_TO_ROOT = "redirect:/";

    private final ExplorerService explorerService;

    @PostMapping("/add-folder")
    public String addFolder(@ModelAttribute("folder-name") String folderName,
                            @ModelAttribute("path") String path,
                            RedirectAttributes redirectAttributes) {
        log.info("Add folder with name {}", folderName);
        explorerService.createDirectory(path, folderName);
        if (!path.isEmpty()) {
            redirectAttributes.addAttribute("path", path);
        }
        return REDIRECT_TO_ROOT;
    }

    @GetMapping("/")
    public String getObjects(Model model, Authentication authentication,
                             @RequestParam(required = false, defaultValue = "") String path) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("breadcrumbs", PathLinksDTOMapper.toChainLinksFromPath(path));
        model.addAttribute("storageObjects", explorerService.getContentOfDirectory(path));
        model.addAttribute("currentPath", path);
        return "explorer/main";
    }

    @PostMapping("/rename-folder")
    public String renameFolder(@ModelAttribute("redirect-path") String parentPath,
                               @ModelAttribute("current-folder-path") String currentFolderPath,
                               @ModelAttribute("new-folder-name") String newFolderName,
                               RedirectAttributes redirectAttributes) {
        log.info("Rename folder with path {} to {}", currentFolderPath, newFolderName);
        explorerService.renameFolder(currentFolderPath, newFolderName);
        if (!parentPath.isEmpty()) {
            redirectAttributes.addAttribute("path", parentPath);
        }
        return REDIRECT_TO_ROOT;
    }

    @PostMapping("/delete-folder")
    public String deleteFolder(@ModelAttribute("redirect-path") String redirectPath,
                               @ModelAttribute("folder-path") String folderPath,
                               RedirectAttributes redirectAttributes) {
        log.info("Delete folder with path {}", folderPath);
        explorerService.deleteFolder(folderPath);
        if (!redirectPath.isEmpty()) {
            redirectAttributes.addAttribute("path", redirectPath);
        }
        return REDIRECT_TO_ROOT;
    }

    @PostMapping("/rename-file")
    public String renameFile(@ModelAttribute("parent-path") String parentPath,
                             @ModelAttribute("current-file-name") String currentFileName,
                             @ModelAttribute("new-file-name") String newFileName,
                             RedirectAttributes redirectAttributes) {
        log.info("Rename file folder {} form {} to {}", parentPath, currentFileName, newFileName);
        explorerService.renameFile(parentPath, currentFileName, newFileName);
        if (!parentPath.isEmpty()) {
            redirectAttributes.addAttribute("path", parentPath);
        }
        return REDIRECT_TO_ROOT;
    }

    @PostMapping("/delete-file")
    public String deleteFile(@ModelAttribute("parent-path") String parentPath,
                             @ModelAttribute("file-name") String fileName,
                             RedirectAttributes redirectAttributes) {
        log.info("Delete file with path {}", fileName);
        explorerService.deleteFile(parentPath, fileName);
        if (!parentPath.isEmpty()) {
            redirectAttributes.addAttribute("path", parentPath);
        }
        return REDIRECT_TO_ROOT;
    }
}
