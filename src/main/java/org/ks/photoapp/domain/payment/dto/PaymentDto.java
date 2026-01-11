package org.ks.photoapp.domain.payment.dto;

public class PaymentDto {
    private Float deposit;
    private Float basePayment;
    private Float additionalPayment;
    private Boolean isDepositPaid;
    private Boolean isBasePaid;
    private Boolean isAdditionalPaid;

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
