package org.ks.photoapp.web.photosession;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.ks.photoapp.web.photosession.PhotoSessionController;
import org.ks.photoapp.domain.photosession.PhotoSessionService;
import org.ks.photoapp.domain.client.ClientService;
import org.ks.photoapp.domain.client.dto.ClientDto;
import org.ks.photoapp.domain.photosession.dto.PhotoSessionDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PhotoSessionController.class)
class PhotoSessionControllerMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PhotoSessionService photoSessionService;

    @MockBean
    ClientService clientService;

    @Test
    void listAllPhotoSessions_displaysAllPhotoSessionsView() throws Exception {
        PhotoSessionDto dto = new PhotoSessionDto();
        dto.setSessionPhotoId(5L);
        when(photoSessionService.getAllSessions()).thenReturn(List.of(dto));

        mockMvc.perform(get("/all-photosessions"))
                .andExpect(status().isOk())
                .andExpect(view().name("all-photosessions"))
                .andExpect(model().attributeExists("photoSessions"))
                .andExpect(model().attribute("heading", "Aktualne sesje"));
    }

    @Test
    void showPhotoSessionByClient_showsPhotoSession() throws Exception {
        long clientId = 2L;
        PhotoSessionDto ps = new PhotoSessionDto();
        ps.setSessionPhotoId(10L);

        when(photoSessionService.getPhotoSessionByClientId(clientId)).thenReturn(Optional.of(ps));

        mockMvc.perform(get("/photosession/" + clientId))
                .andExpect(status().isOk())
                .andExpect(view().name("photosession-client"))
                .andExpect(model().attributeExists("photoSession"));
    }

    @Test
    void newPhotoSessionForm_showsForm() throws Exception {
        when(clientService.getAllClients()).thenReturn(List.of(new ClientDto()));

        mockMvc.perform(get("/new-photosession"))
                .andExpect(status().isOk())
                .andExpect(view().name("photosession-form"))
                .andExpect(model().attributeExists("photoSession"))
                .andExpect(model().attributeExists("clients"))
                .andExpect(model().attributeExists("sessionTypes"));
    }

    @Test
    void createPhotoSession_postRedirectsWithFlash() throws Exception {
        mockMvc.perform(post("/new-photosession")
                        .param("sessionDate", LocalDateTime.now().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/all-photosessions"))
                .andExpect(flash().attributeExists("message"));

        verify(photoSessionService).createNewSession(any(PhotoSessionDto.class));
    }

    @Test
    void deletePhotoSession_redirectsAndAddsFlash() throws Exception {
        mockMvc.perform(get("/delete-photosession/3"))
                .andExpect(status().isOk())
                .andExpect(view().name("delete-photosession"))
                .andExpect(model().attributeExists("id"));

        mockMvc.perform(post("/delete-photosession/3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/all-photosessions"))
                .andExpect(flash().attributeExists("notification"));

        verify(photoSessionService).deleteSession(3L);
    }

}
