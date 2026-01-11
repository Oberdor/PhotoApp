package org.ks.photoapp.domain.client.mapper;

import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.client.dto.ClientDto;
import org.ks.photoapp.domain.photosession.mapper.PhotoSessionDtoMapper;
import org.ks.photoapp.domain.photosession.dto.PhotoSessionDto;

import java.util.List;
import java.util.stream.Collectors;

public class ClientDtoMapper {

    public static ClientDto map(Client client) {
        if (client == null) {
            return null;
        }

        ClientDto clientDto = new ClientDto();
        clientDto.setId(client.getId());
        clientDto.setFirstName(client.getFirstName());
        clientDto.setLastName(client.getLastName());
        clientDto.setEmail(client.getEmail());
        clientDto.setPhoneNumber(client.getPhoneNumber());

        if (client.getPhotoSessions() != null && !client.getPhotoSessions().isEmpty()) {
            List<PhotoSessionDto> photoSessionDto = client.getPhotoSessions().stream()
                    .map(PhotoSessionDtoMapper::map)
                    .collect(Collectors.toList());
            clientDto.setPhotoSessions(photoSessionDto);
        }

        return clientDto;
    }
}
