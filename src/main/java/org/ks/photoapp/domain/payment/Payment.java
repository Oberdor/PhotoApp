package org.ks.photoapp.domain.payment;

import jakarta.persistence.*;
import lombok.Data;
import org.ks.photoapp.domain.photosession.PhotoSession;

@Data
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Float deposit;
    Float basePayment;
    Float additionalPayment;
    Boolean isDepositPaid;
    Boolean isBasePaid;
    Boolean isAdditionalPaid;
    @OneToOne(mappedBy = "payment", cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    PhotoSession photoSession;
    public void applyFromDto(Boolean isDepositPaid, Boolean isBasePaid, Boolean isAdditionalPaid) {
        this.setIsDepositPaid(isDepositPaid);
        this.setIsBasePaid(isBasePaid);
        this.setIsAdditionalPaid(isAdditionalPaid);
    }


}
