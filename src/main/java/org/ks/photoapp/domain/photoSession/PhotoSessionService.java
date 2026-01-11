package org.ks.photoapp.domain.photoSession;


import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.client.ClientDtoMapper;
import org.ks.photoapp.domain.client.ClientRepository;
import org.ks.photoapp.domain.client.ClientService;
import org.ks.photoapp.domain.payment.Payment;
import org.ks.photoapp.domain.payment.PaymentRepository;
import org.ks.photoapp.domain.photoSession.dto.PhotoSessionDto;
import org.ks.photoapp.domain.photos.Photos;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhotoSessionService {
    private final ClientRepository clientRepository;
    private final PhotoSessionRepository photoSessionRepository;


    public PhotoSessionService(PhotoSessionRepository photoSessionRepository, ClientRepository clientRepository) {
        this.photoSessionRepository = photoSessionRepository;
        this.clientRepository = clientRepository;
    }

    public List <PhotoSessionDto> getAll() {
        List<PhotoSession> photoSessions = (List<PhotoSession>) photoSessionRepository.findAll();
        return photoSessions.stream()
            .filter(photoSession -> Boolean.FALSE.equals(photoSession.getIsContractFinished()))
            .map(PhotoSessionDtoMapper::map)
            .toList();
    }


    public PhotoSession findById(Long id) {
        Optional<PhotoSession> optionalPhotoSession = photoSessionRepository.findById(id);
        return optionalPhotoSession.orElseThrow(() -> new NullPointerException("PhotoSession not found with ID: " + id));
    }

    public Optional<PhotoSessionDto> getPhotoSessionByClientId(Long clientId) {
        Optional<PhotoSession> photoSession = photoSessionRepository.findPhotoSessionByClientId(clientId);
        return photoSession.map(PhotoSessionDtoMapper::map);
    }



    public void createNewSession(PhotoSessionDto photoSessionToSave) {
        Payment payment = new Payment();
        Photos photos = new Photos();
        PhotoSession photoSession = new PhotoSession();
        Client client = clientRepository.findById(photoSessionToSave.getClient().getId()).orElse(new Client());
        photoSession.setClient(client);
        photoSession.setPayment(payment);
        photoSession.setPhotos(photos);
        photoSession.setSessionDate(photoSessionToSave.getSessionDate());
        photoSession.setSessionType(photoSessionToSave.getSessionType());
        photoSession.setIsContractFinished(false);
        photoSessionRepository.save(photoSession);
    }

    public void deleteSession(long id) {
        photoSessionRepository.deleteById(id);
    }


    public void updateSession(PhotoSessionDto photoSessionDto, long id) {
        PhotoSession photoSessionToUpdate = photoSessionRepository.findPhotoSessionById(id)
                .orElseThrow(() -> new IllegalArgumentException("Photo session not found"));

        if (photoSessionDto.getClient() == null || photoSessionDto.getClient().getId() == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }

        Client client = clientRepository.findById(photoSessionDto.getClient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        photoSessionToUpdate.setClient(client);
        photoSessionToUpdate.setSessionDate(photoSessionDto.getSessionDate());

        Payment payment = photoSessionToUpdate.getPayment();
        if (payment == null) {
            throw new IllegalStateException("Payment information is missing");
        }

        payment.setIsDepositPaid(photoSessionDto.getIsDepositPaid());
        payment.setIsBasePaid(photoSessionDto.getIsBasePaid());
        payment.setIsAdditionalPaid(photoSessionDto.getIsAdditionalPaid());

        Photos photos = photoSessionToUpdate.getPhotos();
        if (photos == null) {
            throw new IllegalStateException("Photos information is missing");
        }

        photos.setSentToClientForChoose(photoSessionDto.getIsPhotosSentToClientForChoose());
        photos.setChosenByClient(photoSessionDto.getIsPhotosChosenByClient());
        photos.setAdditionalChosenByClient(photoSessionDto.getIsAdditionalPhotosChosenByClient());

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

    public List<PhotoSessionDto> findAllUnfinishedPhotoSession(){
        return photoSessionRepository.findAllByIsContractFinishedIsFalse().stream()
                .map(PhotoSessionDtoMapper::map)
                .toList();
    }


}