package ru.denisovmaksim.cloudfilestorage.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import ru.denisovmaksim.cloudfilestorage.dto.FileType;
import ru.denisovmaksim.cloudfilestorage.dto.StorageObjectDTO;
import ru.denisovmaksim.cloudfilestorage.service.SearchService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @InjectMocks
    private SearchController searchController;

    @Mock
    private SearchService searchService;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @Test
    void getObjectsShouldAddAttributesToModelAndReturnViewName() {
        String query = "test";
        String username = "testUser";
        StorageObjectDTO firstDTO =
                new StorageObjectDTO("file1.txt", "file1.txt", FileType.UNKNOWN_FILE, 100L);
        StorageObjectDTO secondDTO =
                new StorageObjectDTO("file2.txt", "file2.txt", FileType.UNKNOWN_FILE, 100L);

        List<StorageObjectDTO> mockResults = List.of(firstDTO, secondDTO);

        Mockito.when(authentication.getName()).thenReturn(username);
        Mockito.when(searchService.search(query)).thenReturn(mockResults);

        String viewName = searchController.getObjects(model, authentication, query);

        assertEquals("search/search", viewName);
        Mockito.verify(model).addAttribute("username", username);
        Mockito.verify(model).addAttribute("storageObjects", mockResults);
        Mockito.verify(searchService).search(query);
    }
}
