package org.ks.photoapp.domain.payment.dto;

import lombok.Data;

@Data
public class PaymentDto {
    Float deposit;
    Float basePayment;
    Float additionalPayment;
    Boolean isDepositPaid;
    Boolean isBasePaid;
    Boolean isAdditionalPaid;

    public PaymentDto(Float deposit, Float basePayment, Float additionalPayment, Boolean isDepositPaid,
                      Boolean isBasePaid, Boolean isAdditionalPaid) {
        this.deposit = deposit;
        this.basePayment = basePayment;
        this.additionalPayment = additionalPayment;
        this.isDepositPaid = isDepositPaid;
        this.isBasePaid = isBasePaid;
        this.isAdditionalPaid = isAdditionalPaid;
    }
}
