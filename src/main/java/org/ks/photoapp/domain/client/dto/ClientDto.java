package org.ks.photoapp.domain.client.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.ks.photoapp.domain.photoSession.PhotoSession;
import org.ks.photoapp.domain.photoSession.dto.PhotoSessionDto;

import java.util.List;

@Data
public class ClientDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Long phoneNumber;
    private List<PhotoSessionDto> photoSessions;


    public ClientDto() {
    }

    public ClientDto(Long id, Long phoneNumber, String email, String lastName, String firstName) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.lastName = lastName;
        this.firstName = firstName;
    }

}
