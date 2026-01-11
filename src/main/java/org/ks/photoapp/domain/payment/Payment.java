package org.ks.photoapp.domain.payment;

import jakarta.persistence.*;
import lombok.Data;
import org.ks.photoapp.domain.photoSession.PhotoSession;

@Data
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Float deposit;
    private Float basePayment;
    private Float additionalPayment;
    private Boolean isDepositPaid;
    private Boolean isBasePaid;
    private Boolean isAdditionalPaid;
    @OneToOne(mappedBy = "payment", cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    private PhotoSession photoSession;



}
