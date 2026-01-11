package org.ks.photoapp.domain.client;

import org.ks.photoapp.domain.client.dto.ClientDto;

public class ClientDtoMapper {

    public static ClientDto map(Client client) {
        return org.ks.photoapp.domain.client.mapper.ClientDtoMapper.map(client);
    }
}
