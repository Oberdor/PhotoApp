package org.ks.photoapp.domain.photosession;


import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.client.mapper.ClientDtoMapper;
import org.ks.photoapp.domain.client.ClientRepository;
import org.ks.photoapp.domain.client.ClientService;
import org.ks.photoapp.domain.payment.Payment;
import org.ks.photoapp.domain.payment.PaymentRepository;
import org.ks.photoapp.domain.photosession.dto.PhotoSessionDto;
import org.ks.photoapp.domain.photosession.mapper.PhotoSessionDtoMapper;
import org.ks.photoapp.domain.photosession.exception.PhotoSessionNotFoundException;
import org.ks.photoapp.domain.client.exception.ClientNotFoundException;
import org.ks.photoapp.domain.photos.Photos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhotoSessionService {
    private final ClientRepository clientRepository;
    PhotoSessionRepository photoSessionRepository;


    public PhotoSessionService(PhotoSessionRepository photoSessionRepository, ClientRepository clientRepository) {
        this.photoSessionRepository = photoSessionRepository;
        this.clientRepository = clientRepository;
    }

    public List <PhotoSessionDto> getAllSessions() {
        List<PhotoSession> photoSessions = (List<PhotoSession>) photoSessionRepository.findAll();
        return photoSessions.stream()
                .filter(photoSession -> !photoSession.isContractFinished)
                .map(PhotoSessionDtoMapper::map)
                .toList();
    }


    public PhotoSession findById(Long id) {
        Optional<PhotoSession> optionalPhotoSession = photoSessionRepository.findById(id);
        return optionalPhotoSession.orElseThrow(() -> new PhotoSessionNotFoundException(id));
    }

    public Optional<PhotoSessionDto> getPhotoSessionByClientId(Long clientId) {
        Optional<PhotoSession> photoSession = photoSessionRepository.findPhotoSessionByClientId(clientId);
        return photoSession.map(PhotoSessionDtoMapper::map);
    }



    public void createNewSession(PhotoSessionDto photoSessionToSave) {
        Payment payment = new Payment();
        Photos photos = new Photos();
        PhotoSession photoSession = new PhotoSession();
        Client client;
        if (photoSessionToSave.getClient() != null && photoSessionToSave.getClient().getId() != null) {
            client = clientRepository.findById(photoSessionToSave.getClient().getId())
                    .orElseThrow(() -> new ClientNotFoundException(photoSessionToSave.getClient().getId()));
        } else {
            client = new Client();
        }
        photoSession.setClient(client);
        photoSession.setPayment(payment);
        photoSession.setPhotos(photos);
        photoSession.setSessionDate(photoSessionToSave.getSessionDate());
        photoSession.setSessionType(photoSessionToSave.getSessionType());
        photoSession.isContractFinished = false;
        photoSessionRepository.save(photoSession);
    }

    public void deleteSession(long id) {
        if (!photoSessionRepository.existsById(id)) {
            throw new PhotoSessionNotFoundException(id);
        }
        photoSessionRepository.deleteById(id);
    }


    public void updateSession(PhotoSessionDto photoSessionDto, long id) {
        PhotoSession photoSessionToUpdate = loadSessionOrThrow(id);

        Client client = loadClientOrThrow(photoSessionDto);

        photoSessionToUpdate.applyUpdateFrom(photoSessionDto, client);

        photoSessionRepository.save(photoSessionToUpdate);
    }


//
//    public void updateSession(PhotoSessionDto photoSessionDto, long id){
//        PhotoSession photoSessionToUpdate = photoSessionRepository.findPhotoSessionById(id).orElseGet(PhotoSession::new);
//        photoSessionToUpdate.setClient(photoSessionDto.getClient());
//        photoSessionToUpdate.setSessionDate(photoSessionDto.getSessionDate());
//        photoSessionToUpdate.setSessionType(photoSessionDto.getSessionType());
//        photoSessionToUpdate.getPayment().setIsDepositPaid(photoSessionDto.getIsDepositPaid());
//        photoSessionToUpdate.getPayment().setIsBasePaid(photoSessionDto.getIsBasePaid());
//        photoSessionToUpdate.getPhotos().setSentToClientForChoose(photoSessionDto.getIsPhotosSentToClientForChoose());
//        photoSessionToUpdate.getPhotos().setChosenByClient(photoSessionDto.getIsPhotosChosenByClient());
//        photoSessionToUpdate.getPhotos().setAdditionalChosenByClient(photoSessionDto.getIsAdditionalPhotosChosenByClient());
//        photoSessionToUpdate.getPayment().setIsAdditionalPaid(photoSessionDto.getIsAdditionalPaid());
//        photoSessionToUpdate.setIsContractFinished(photoSessionDto.getIsContractFinished());
//        photoSessionRepository.save(photoSessionToUpdate);
//    }

    public Optional<PhotoSessionDto> getPhotoSessionByClient(Client client){
        return photoSessionRepository.findPhotoSessionByClient(client)
                .map(PhotoSessionDtoMapper::map);
    }

    public List<Client> getClientsByDate(LocalDateTime date) {
        Optional<PhotoSession> sessions = photoSessionRepository.findPhotoSessionBySessionDate(date);
        return sessions.stream()
                .map(PhotoSession::getClient)
                .collect(Collectors.toList());
    }

    public Optional<PhotoSessionDto> getPhotoSessionByDate(LocalDateTime date){
        return photoSessionRepository.findPhotoSessionBySessionDate(date)
                .map(PhotoSessionDtoMapper::map);
    }

    public List<PhotoSessionDto> getAllUnfinishedSessions(){
        return photoSessionRepository.findAllByIsContractFinishedIsFalse().stream()
                .map(PhotoSessionDtoMapper::map)
                .toList();
    }

    private PhotoSession loadSessionOrThrow(long id) {
        return photoSessionRepository.findPhotoSessionById(id)
                .orElseThrow(() -> new PhotoSessionNotFoundException(id));
    }

    private Client loadClientOrThrow(PhotoSessionDto dto) {
        if (dto.getClient() == null || dto.getClient().getId() == null) {
            throw new ClientNotFoundException("Client ID cannot be null");
        }
        return clientRepository.findById(dto.getClient().getId())
                .orElseThrow(() -> new ClientNotFoundException(dto.getClient().getId()));
    }

    


}