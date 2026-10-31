package com.skillbridge.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicHomeIsAccessible() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SkillBridge")))
                .andExpect(content().string(containsString("Find your people. Build what matters.")));
    }

    @Test
    void publicLoginIsAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign in to your SkillBridge")));
    }

    @Test
    void unauthenticatedUserRedirectedFromProjects() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "priya@skillbridge.edu", roles = "STUDENT")
    void authenticatedUserCanAccessDashboardAndProjects() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Priya Patel")));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SkillBridge Platform")));
    }
}

