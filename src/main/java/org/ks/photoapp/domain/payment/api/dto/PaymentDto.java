package org.ks.photoapp.domain.payment.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentDto {

    private Long id;

    private Float deposit;

    private Float basePayment;

    private Float additionalPayment;

    @JsonProperty("isDepositPaid")
    private Boolean isDepositPaid;

    @JsonProperty("isBasePaid")
    private Boolean isBasePaid;

    @JsonProperty("isAdditionalPaid")
    private Boolean isAdditionalPaid;

    private Long photoSessionId;

    @JsonProperty("isContractFinished")
    private Boolean isContractFinished;

    public PaymentDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getDeposit() {
        return deposit;
    }

    public void setDeposit(Float deposit) {
        this.deposit = deposit;
    }

    public Float getBasePayment() {
        return basePayment;
    }

    public void setBasePayment(Float basePayment) {
        this.basePayment = basePayment;
    }

    public Float getAdditionalPayment() {
        return additionalPayment;
    }

    public void setAdditionalPayment(Float additionalPayment) {
        this.additionalPayment = additionalPayment;
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

    public Long getPhotoSessionId() {
        return photoSessionId;
    }

    public void setPhotoSessionId(Long photoSessionId) {
        this.photoSessionId = photoSessionId;
    }

    public Boolean getIsContractFinished() {
        return isContractFinished;
    }

    public void setIsContractFinished(Boolean isContractFinished) {
        this.isContractFinished = isContractFinished;
    }
}
