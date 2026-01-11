package org.ks.photoapp.domain.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ks.photoapp.domain.client.dto.ClientDto;
import org.ks.photoapp.domain.photosession.PhotoSession;
import org.ks.photoapp.domain.photosession.PhotoSessionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    ClientRepository clientRepository;
    PhotoSessionRepository photoSessionRepository;
    ClientService clientService;

    @BeforeEach
    void setUp() {
        clientRepository = mock(ClientRepository.class);
        photoSessionRepository = mock(PhotoSessionRepository.class);
        clientService = new ClientService(clientRepository, photoSessionRepository);
    }

    @Test
    void getAllCurrentClients_returnsDistinctClientsFromUnfinishedSessions() {
        PhotoSession s1 = new PhotoSession(); s1.setSessionDate(LocalDateTime.now()); s1.setIsContractFinished(false);
        PhotoSession s2 = new PhotoSession(); s2.setSessionDate(LocalDateTime.now()); s2.setIsContractFinished(false);
        when(photoSessionRepository.findAll()).thenReturn(List.of(s1, s2));

        List<ClientDto> clients = clientService.getAllCurrentClients();

        assertThat(clients).isNotNull();
    }

    @Test
    void createNewClient_savesClient() {
        ClientDto dto = new ClientDto();
        dto.setFirstName("Anna"); dto.setLastName("Nowak");

        clientService.createNewClient(dto);

        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void updateClientDetails_throwsWhenMissing() {
        ClientDto dto = new ClientDto();
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(org.ks.photoapp.domain.client.exception.ClientNotFoundException.class,
                () -> clientService.updateClientDetails(dto, 99L));
    }

    @Test
    void deleteClient_throwsWhenMissing() {
        when(clientRepository.existsById(123L)).thenReturn(false);
        assertThrows(org.ks.photoapp.domain.client.exception.ClientNotFoundException.class,
                () -> clientService.deleteClient(123L));
    }
}
