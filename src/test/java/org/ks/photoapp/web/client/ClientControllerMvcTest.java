package org.ks.photoapp.web.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.ks.photoapp.web.client.ClientController;
import org.ks.photoapp.domain.client.ClientService;
import org.ks.photoapp.domain.client.dto.ClientDto;
import org.ks.photoapp.domain.photosession.PhotoSessionService;
import org.ks.photoapp.domain.photosession.dto.PhotoSessionDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ClientController.class)
class ClientControllerMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ClientService clientService;

    @MockBean
    PhotoSessionService photoSessionService;

    @Test
    void listAllClients_displaysAllClientsView() throws Exception {
        ClientDto dto = new ClientDto();
        dto.setId(1L);
        when(clientService.getAllClients()).thenReturn(List.of(dto));

        mockMvc.perform(get("/client/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("all-clients"))
                .andExpect(model().attributeExists("clients"))
                .andExpect(model().attribute("heading", "Wszyscy klienci"));
    }

    @Test
    void showClient_showsClientAndPhotoSession() throws Exception {
        long id = 7L;
        ClientDto clientDto = new ClientDto();
        clientDto.setId(id);
        PhotoSessionDto ps = new PhotoSessionDto();
        ps.setSessionPhotoId(12L);

        when(clientService.findClientById(id)).thenReturn(Optional.of(clientDto));
        when(photoSessionService.getPhotoSessionByClientId(id)).thenReturn(Optional.of(ps));

        mockMvc.perform(get("/client/" + id))
                .andExpect(status().isOk())
                .andExpect(view().name("client"))
                .andExpect(model().attributeExists("client"))
                .andExpect(model().attributeExists("photoSession"));
    }

    @Test
    void newClientForm_showsForm() throws Exception {
        mockMvc.perform(get("/client/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("client-form"))
                .andExpect(model().attributeExists("client"));
    }

    @Test
    void createClient_postRedirectsWithFlash() throws Exception {
        mockMvc.perform(post("/client/new")
                        .param("firstName", "Jan")
                        .param("lastName", "Kowalski"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/client/all"))
                .andExpect(flash().attributeExists("notification"));

        verify(clientService).createNewClient(any(ClientDto.class));
    }

    @Test
    void deleteClient_redirectsAndAddsFlash() throws Exception {
        mockMvc.perform(get("/client/delete").param("id", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/all-clients"))
                .andExpect(flash().attributeExists("notification"));

        verify(clientService).deleteClient(3L);
    }

}
