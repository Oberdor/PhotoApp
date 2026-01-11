package org.ks.photoapp.domain.client;


import org.ks.photoapp.domain.client.dto.ClientDto;
import org.ks.photoapp.domain.photosession.PhotoSession;
import org.ks.photoapp.domain.photosession.PhotoSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ks.photoapp.domain.client.exception.ClientNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

   private final ClientRepository clientRepository;
   private final PhotoSessionRepository photoSessionRepository;

    public ClientService(ClientRepository clientRepository, PhotoSessionRepository photoSessionRepository) {
        this.clientRepository = clientRepository;
        this.photoSessionRepository = photoSessionRepository;
    }


    public List<ClientDto> getAllCurrentClients() {
        List<PhotoSession> photoSessions = (List<PhotoSession>) photoSessionRepository.findAll();
        return photoSessions.stream()
                .filter(session -> !session.getIsContractFinished())
                .map(PhotoSession::getClient)
                .distinct()
                .map(org.ks.photoapp.domain.client.mapper.ClientDtoMapper::map)
                .toList();
    }
    public List<ClientDto> getAllClients() {
       List<Client> clients = (List<Client>)clientRepository.findAll();
               return clients.stream()
                   .map(org.ks.photoapp.domain.client.mapper.ClientDtoMapper::map)
                   .toList();
    }

    public Optional<ClientDto> findClientById(long id){
        return clientRepository.findById(id)
            .map(org.ks.photoapp.domain.client.mapper.ClientDtoMapper::map);
    }

    public Optional<ClientDto> findClientByLastName(String lastName){
        return clientRepository.findByLastName(lastName)
            .map(org.ks.photoapp.domain.client.mapper.ClientDtoMapper::map);
    }

    @Transactional
    public void createNewClient(ClientDto clientDto){
        Client client = new Client();
        client.setLastName(clientDto.getLastName());
        client.setFirstName(clientDto.getFirstName());
        client.setEmail(clientDto.getEmail());
        client.setPhoneNumber(clientDto.getPhoneNumber());
        clientRepository.save(client);
    }

    public void updateClientDetails(ClientDto clientDto, long id) {
    Client clientToUpdate = clientRepository.findById(id)
            .orElseThrow(() -> new ClientNotFoundException(id));
    clientToUpdate.setLastName(clientDto.getLastName());
    clientToUpdate.setFirstName(clientDto.getFirstName());
    clientToUpdate.setEmail(clientDto.getEmail());
    clientToUpdate.setPhoneNumber(clientDto.getPhoneNumber());
        clientRepository.save(clientToUpdate);
    }


    public void deleteClient(Long id){
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException(id);
        }
        clientRepository.deleteClientById(id);
    }
}
