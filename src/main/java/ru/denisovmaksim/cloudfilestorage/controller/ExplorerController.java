package ru.denisovmaksim.cloudfilestorage.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.denisovmaksim.cloudfilestorage.mapper.PathLinksDTOMapper;
import ru.denisovmaksim.cloudfilestorage.service.ExplorerService;
import ru.denisovmaksim.cloudfilestorage.util.PathUtil;


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
        if (!path.isEmpty()) {
            redirectAttributes.addAttribute("path", path);
        }
        explorerService.createFolder(path + folderName);
        return REDIRECT_TO_ROOT;
    }

    @GetMapping("/")
    public String getObjects(Model model, @RequestParam(required = false, defaultValue = "") String path) {
        model.addAttribute("breadcrumbs", PathLinksDTOMapper.toChainLinksFromPath(path));
        model.addAttribute("storageObjects", explorerService.getFolder(path));
        model.addAttribute("currentPath", path);
        return "explorer/content";
    }

    @PostMapping("/rename-folder")
    public String renameFolder(@ModelAttribute("folder-path") String folderPath,
                               @ModelAttribute("new-folder-name") String newFolderName,
                               RedirectAttributes redirectAttributes) {
        String parentPath = PathUtil.getParentDirName(folderPath);
        log.info("Rename folder with path {} to {}", folderPath, parentPath + newFolderName);
        redirectAttributes.addAttribute("path", parentPath);
        explorerService.renameFolder(folderPath, newFolderName);
        return REDIRECT_TO_ROOT;
    }

    @PostMapping("/delete-folder")
    public String deleteFolder(@ModelAttribute("folder-path") String folderPath,
                               RedirectAttributes redirectAttributes) {
        log.info("Delete folder with path {}", folderPath);
        String redirectPath = PathUtil.getParentDirName(folderPath);
        redirectAttributes.addAttribute("path", redirectPath);
        explorerService.deleteFolder(folderPath);
        return REDIRECT_TO_ROOT;
    }

    @PostMapping("/rename-file")
    public String renameFile(@ModelAttribute("filepath") String path,
                             @ModelAttribute("name") String newFileName,
                             RedirectAttributes redirectAttributes) {
        String parentPath = PathUtil.getParentDirName(path);
        log.info("Rename file from {} to {}", path, parentPath + newFileName);
        redirectAttributes.addAttribute("path", parentPath);
        explorerService.renameFile(path, newFileName);
        return REDIRECT_TO_ROOT;
    }

    @PostMapping("/delete-file")
    public String deleteFile(@ModelAttribute("filepath") String filepath, RedirectAttributes redirectAttributes) {
        log.info("Delete file with path {}", filepath);
        String redirectPath = PathUtil.getParentDirName(filepath);
        redirectAttributes.addAttribute("path", redirectPath);
        explorerService.deleteFile(filepath);
        return REDIRECT_TO_ROOT;
    }
}
