package org.ks.photoapp.domain.payment.api.dto;

public class PaymentPatchRequest {
    private Boolean isDepositPaid;
    private Boolean isBasePaid;
    private Boolean isAdditionalPaid;

    public PaymentPatchRequest() {
    }

    public Boolean getIsDepositPaid() {
        return isDepositPaid;
    }

    public void setIsDepositPaid(Boolean isDepositPaid) {
        this.isDepositPaid = isDepositPaid;
    }

    public Boolean getIsBasePaid() {
        return isBasePaid;
    }

    public void setIsBasePaid(Boolean isBasePaid) {
        this.isBasePaid = isBasePaid;
    }

    public Boolean getIsAdditionalPaid() {
        return isAdditionalPaid;
    }

    public void setIsAdditionalPaid(Boolean isAdditionalPaid) {
        this.isAdditionalPaid = isAdditionalPaid;
    }
}
