package org.ks.photoapp.domain.photosession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.client.ClientRepository;
import org.ks.photoapp.domain.photosession.dto.PhotoSessionDto;
import org.ks.photoapp.domain.payment.Payment;
import org.ks.photoapp.domain.photos.Photos;
import org.ks.photoapp.domain.sessionType.SessionType;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoSessionServiceTest {

    @Mock
    PhotoSessionRepository photoSessionRepository;

    @Mock
    ClientRepository clientRepository;

    @InjectMocks
    PhotoSessionService photoSessionService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createNewSession_savesPhotoSession() {
        Client client = new Client();
        client.setId(1L);

        PhotoSessionDto dto = new PhotoSessionDto();
        dto.setClient(client);
        dto.setSessionDate(LocalDateTime.of(2026,1,1,10,0));
        dto.setSessionType(SessionType.WEDDING);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        photoSessionService.createNewSession(dto);

        ArgumentCaptor<PhotoSession> captor = ArgumentCaptor.forClass(PhotoSession.class);
        verify(photoSessionRepository).save(captor.capture());

        PhotoSession saved = captor.getValue();
        assertThat(saved.getClient()).isEqualTo(client);
        assertThat(saved.getPayment()).isNotNull();
        assertThat(saved.getPhotos()).isNotNull();
        assertThat(saved.getSessionDate()).isEqualTo(dto.getSessionDate());
        assertThat(saved.getSessionType()).isEqualTo(dto.getSessionType());
        assertThat(saved.getIsContractFinished()).isFalse();
    }

    @Test
    void updateSession_happyPath_updatesFields() {
        long id = 5L;

        Client existingClient = new Client();
        existingClient.setId(2L);

        PhotoSession existing = new PhotoSession();
        existing.setId(id);
        existing.setClient(existingClient);
        existing.setPayment(new Payment());
        existing.setPhotos(new Photos());

        when(photoSessionRepository.findPhotoSessionById(id)).thenReturn(Optional.of(existing));

        Client newClient = new Client();
        newClient.setId(3L);
        when(clientRepository.findById(3L)).thenReturn(Optional.of(newClient));

        PhotoSessionDto dto = new PhotoSessionDto();
        Client dtoClient = new Client();
        dtoClient.setId(3L);
        dto.setClient(dtoClient);
        dto.setSessionDate(LocalDateTime.of(2026,2,2,15,0));
        dto.setIsDepositPaid(true);
        dto.setIsBasePaid(true);
        dto.setIsAdditionalPaid(false);
        dto.setIsPhotosSentToClientForChoose(true);
        dto.setIsPhotosChosenByClient(false);
        dto.setIsAdditionalPhotosChosenByClient(false);

        photoSessionService.updateSession(dto, id);

        assertThat(existing.getClient()).isEqualTo(newClient);
        assertThat(existing.getSessionDate()).isEqualTo(dto.getSessionDate());
        assertThat(existing.getPayment().getIsDepositPaid()).isTrue();
        assertThat(existing.getPayment().getIsBasePaid()).isTrue();
        assertThat(existing.getPayment().getIsAdditionalPaid()).isFalse();
        assertThat(existing.getPhotos().getSentToClientForChoose()).isTrue();

        verify(photoSessionRepository).save(existing);
    }

    @Test
    void getAll_returnsOnlyUnfinishedSessions() {
        PhotoSession finished = new PhotoSession();
        finished.setId(10L);
        finished.setPayment(new Payment());
        finished.setPhotos(new Photos());
        finished.setIsContractFinished(true);

        PhotoSession unfinished = new PhotoSession();
        unfinished.setId(11L);
        unfinished.setPayment(new Payment());
        unfinished.setPhotos(new Photos());
        unfinished.setIsContractFinished(false);

        when(photoSessionRepository.findAll()).thenReturn(List.of(finished, unfinished));

        List<org.ks.photoapp.domain.photosession.dto.PhotoSessionDto> result = photoSessionService.getAllSessions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSessionPhotoId()).isEqualTo(11L);
    }

}
