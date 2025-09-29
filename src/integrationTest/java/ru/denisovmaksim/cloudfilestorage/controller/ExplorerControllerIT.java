package ru.denisovmaksim.cloudfilestorage.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.denisovmaksim.cloudfilestorage.IntegrationTestConfiguration;
import ru.denisovmaksim.cloudfilestorage.service.SecurityService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest()
@ActiveProfiles("it")
@AutoConfigureMockMvc(addFilters = false)
@Import(IntegrationTestConfiguration.class)
public class ExplorerControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityService securityService;

    @BeforeEach
    public void beforeEach() {
        Mockito.when(securityService.getAuthUserId())
                .thenReturn(1L);
    }

    @Test
    void shouldRenderIndexPage() throws Exception {
        mockMvc.perform(get("/").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("explorer/content"));

    }
}
