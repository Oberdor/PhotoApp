package org.ks.photoapp.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class CustomSecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void loginPageAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login-form"));
    }

    @Test
    void staticResourcesAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/img/some-nonexistent.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sampleAppUrlAccessible_givenPermitAll() throws Exception {
        mockMvc.perform(get("/home-page"))
                .andExpect(status().isOk())
                .andExpect(view().name("home-page"));
    }

}
