package org.ks.photoapp.domain.payment;

import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.client.ClientRepository;
import org.ks.photoapp.domain.payment.api.dto.PaymentPatchRequest;
import org.ks.photoapp.domain.payment.api.dto.PaymentDto;
import org.ks.photoapp.domain.payment.api.mapper.PaymentMapper;
import org.ks.photoapp.domain.photoSession.PhotoSession;
import org.ks.photoapp.domain.photoSession.PhotoSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentService {

    PaymentRepository paymentRepository;
    PhotoSessionRepository photoSessionRepository;


    public PaymentService(PaymentRepository paymentRepository, PhotoSessionRepository photoSessionRepository) {
        this.paymentRepository = paymentRepository;
        this.photoSessionRepository = photoSessionRepository;
    }

    public void updatePayment(long paymentId, Client client) {
        Payment paymentToUpdate = paymentRepository.findPaymentById(paymentId).orElseThrow();
        PhotoSession photoSession = photoSessionRepository.findPhotoSessionByClient(client).orElseThrow();
        paymentToUpdate.setDeposit(photoSession.getPayment().getDeposit());
        paymentToUpdate.setBasePayment(photoSession.getPayment().getBasePayment());
        paymentToUpdate.setAdditionalPayment(photoSession.getPayment().getAdditionalPayment());
        paymentToUpdate.setIsDepositPaid(photoSession.getPayment().getIsDepositPaid());
        paymentToUpdate.setIsBasePaid(photoSession.getPayment().getIsBasePaid());
        paymentToUpdate.setIsAdditionalPaid(photoSession.getPayment().getIsAdditionalPaid());
        paymentRepository.save(paymentToUpdate);
    }

    @Transactional
    public PaymentDto patchPayment(Long paymentId, PaymentPatchRequest request) {
        if (request == null) {
            throw new InvalidPaymentPatchRequestException("Request body must contain at least one updatable field");
        }

        boolean hasAny = request.getIsDepositPaid() != null || request.getIsBasePaid() != null || request.getIsAdditionalPaid() != null;
        if (!hasAny) {
            throw new InvalidPaymentPatchRequestException("Request body must contain at least one updatable field");
        }

        Payment payment = paymentRepository.findPaymentById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        PhotoSession photoSession = payment.getPhotoSession();
        if (photoSession != null && Boolean.TRUE.equals(photoSession.getIsContractFinished())) {
            throw new ContractAlreadyFinishedException("Contract already finished for this payment");
        }

        if (request.getIsDepositPaid() != null) {
            payment.setIsDepositPaid(request.getIsDepositPaid());
        }
        if (request.getIsBasePaid() != null) {
            payment.setIsBasePaid(request.getIsBasePaid());
        }
        if (request.getIsAdditionalPaid() != null) {
            payment.setIsAdditionalPaid(request.getIsAdditionalPaid());
        }

        paymentRepository.save(payment);

        boolean allPaid = Boolean.TRUE.equals(payment.getIsDepositPaid())
                && Boolean.TRUE.equals(payment.getIsBasePaid())
                && Boolean.TRUE.equals(payment.getIsAdditionalPaid());

        if (allPaid && photoSession != null && !Boolean.TRUE.equals(photoSession.getIsContractFinished())) {
            photoSession.setIsContractFinished(true);
            photoSessionRepository.save(photoSession);
        }

        return PaymentMapper.toDto(payment);
    }

}
