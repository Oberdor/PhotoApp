package org.ks.photoapp.domain.payment.api.mapper;

import org.ks.photoapp.domain.payment.Payment;
import org.ks.photoapp.domain.payment.api.dto.PaymentDto;

public class PaymentMapper {

    public static PaymentDto toDto(Payment payment) {
        if (payment == null) return null;
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setDeposit(payment.getDeposit());
        dto.setBasePayment(payment.getBasePayment());
        dto.setAdditionalPayment(payment.getAdditionalPayment());
        dto.setIsDepositPaid(payment.getIsDepositPaid());
        dto.setIsBasePaid(payment.getIsBasePaid());
        dto.setIsAdditionalPaid(payment.getIsAdditionalPaid());
        if (payment.getPhotoSession() != null) {
            dto.setPhotoSessionId(payment.getPhotoSession().getId());
            dto.setIsContractFinished(payment.getPhotoSession().getIsContractFinished());
        }
        return dto;
    }
}
