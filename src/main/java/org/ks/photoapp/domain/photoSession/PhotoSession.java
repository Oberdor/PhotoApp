package org.ks.photoapp.domain.photoSession;

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
    private Long id;
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;
    private LocalDateTime sessionDate;
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "payment_id")
    private Payment payment;
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "photos_id")
    private Photos photos;
    private Boolean isContractFinished;

    public PhotoSession() {
    }

}
