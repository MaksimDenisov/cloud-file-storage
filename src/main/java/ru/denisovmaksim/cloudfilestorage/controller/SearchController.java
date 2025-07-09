package ru.denisovmaksim.cloudfilestorage.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.denisovmaksim.cloudfilestorage.service.SearchService;

@Controller
@AllArgsConstructor
@Slf4j
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("")
    public String getObjects(Model model, Authentication authentication,
                             @RequestParam("query") String query) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("storageObjects", searchService.search(query));
        return "search/main";
    }

}
