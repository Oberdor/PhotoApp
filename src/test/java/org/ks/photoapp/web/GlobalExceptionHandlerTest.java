package org.ks.photoapp.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.ks.photoapp.web.client.ClientController;
import org.ks.photoapp.domain.client.ClientService;
import org.ks.photoapp.domain.client.exception.ClientNotFoundException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = ClientController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ClientService clientService;

    @Test
    void clientNotFound_returns404View() throws Exception {
        when(clientService.findClientById(99L)).thenThrow(new ClientNotFoundException(99L));

        mockMvc.perform(get("/client/99"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

}
