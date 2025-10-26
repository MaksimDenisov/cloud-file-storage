package ru.denisovmaksim.cloudfilestorage.controller;

import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.denisovmaksim.cloudfilestorage.IntegrationTestConfiguration;
import ru.denisovmaksim.cloudfilestorage.service.SecurityService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest()
@ActiveProfiles("it")
@AutoConfigureMockMvc(addFilters = false)
@Import(IntegrationTestConfiguration.class)
public class ExplorerControllerIT {

    @Value("${app.bucket}")
    private String bucket;

    private final String user1Folder = "user-1-files/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MinioClient minioClient;

    @MockBean
    private SecurityService securityService;

    @BeforeEach
    public void beforeEach() {
        Mockito.when(securityService.getAuthUserId())
                .thenReturn(1L);
    }

    @Test
    void shouldCreateFolderAndShowInExplorer() throws Exception {
        mockMvc.perform(post("/add-folder")
                        .param("folder-name", "test-folder")
                        .param("path", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(user1Folder + "test-folder/")
                        .build());

    }

    @Test
    void shouldRenameFolder() throws Exception {
        mockMvc.perform(post("/add-folder")
                .param("folder-name", "old-folder")
                .param("path", ""));

        mockMvc.perform(post("/rename-folder")
                        .param("folder-path", "old-folder/")
                        .param("new-folder-name", "new-folder"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(user1Folder + "new-folder/")
                        .build()
        );

        assertThatThrownBy(() -> minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(user1Folder + "old-folder/")
                        .build()
        )).isInstanceOf(ErrorResponseException.class);
    }

    @Test
    void shouldDeleteFolder() throws Exception {
        mockMvc.perform(post("/add-folder")
                .param("folder-name", "to-delete")
                .param("path", ""));

        mockMvc.perform(post("/delete-folder").with(csrf())
                        .param("folder-path", "to-delete/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThatThrownBy(() -> minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(user1Folder + "to-delete/")
                        .build()
        )).isInstanceOf(ErrorResponseException.class);
    }

    @Test
    void shouldRenderExplorerContent() throws Exception {
        mockMvc.perform(get("/").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("explorer/content"))
                .andExpect(model().attributeExists("storageObjects"))
                .andExpect(model().attributeExists("breadcrumbs"))
                .andExpect(model().attributeExists("currentPath"));
    }
}
