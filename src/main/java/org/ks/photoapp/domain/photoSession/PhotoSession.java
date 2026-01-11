package org.ks.photoapp.domain.photosession;

import jakarta.persistence.*;
import lombok.Data;
import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.payment.Payment;
import org.ks.photoapp.domain.photos.Photos;
import org.ks.photoapp.domain.sessionType.SessionType;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
public class PhotoSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    Client client;
    LocalDateTime sessionDate;
    @Enumerated(EnumType.STRING)
    SessionType sessionType;
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "payment_id")
    Payment payment;
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "photos_id")
    Photos photos;
    Boolean isContractFinished;

    public PhotoSession() {
    }

    public void applyUpdateFrom(org.ks.photoapp.domain.photosession.dto.PhotoSessionDto dto, org.ks.photoapp.domain.client.Client client) {
        if (client != null) {
            this.setClient(client);
        }
        this.setSessionDate(dto.getSessionDate());
        this.setSessionType(dto.getSessionType());

        if (this.getPayment() != null) {
            this.getPayment().applyFromDto(dto.getIsDepositPaid(), dto.getIsBasePaid(), dto.getIsAdditionalPaid());
        }

        if (this.getPhotos() != null) {
            this.getPhotos().applyFromDto(dto.getIsPhotosSentToClientForChoose(), dto.getIsPhotosChosenByClient(), dto.getIsAdditionalPhotosChosenByClient());
        }
    }

}
