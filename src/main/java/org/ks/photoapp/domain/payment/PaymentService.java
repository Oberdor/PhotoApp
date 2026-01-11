package org.ks.photoapp.domain.payment;

import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.client.ClientRepository;
import org.ks.photoapp.domain.payment.dto.PaymentDto;
import org.ks.photoapp.domain.photoSession.PhotoSession;
import org.ks.photoapp.domain.photoSession.PhotoSessionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PhotoSessionRepository photoSessionRepository;

    public PaymentService(PaymentRepository paymentRepository, PhotoSessionRepository photoSessionRepository) {
        this.paymentRepository = paymentRepository;
        this.photoSessionRepository = photoSessionRepository;
    }

    public void updatePayment(long paymentId, long clientId) {
        Payment paymentToUpdate = paymentRepository.findPaymentById(paymentId).orElseThrow();
        PhotoSession photoSession = photoSessionRepository.findPhotoSessionByClientId(clientId).orElseThrow();
        paymentToUpdate.setDeposit(photoSession.getPayment().getDeposit());
        paymentToUpdate.setBasePayment(photoSession.getPayment().getBasePayment());
        paymentToUpdate.setAdditionalPayment(photoSession.getPayment().getAdditionalPayment());
        paymentToUpdate.setIsDepositPaid(photoSession.getPayment().getIsDepositPaid());
        paymentToUpdate.setIsBasePaid(photoSession.getPayment().getIsBasePaid());
        paymentToUpdate.setIsAdditionalPaid(photoSession.getPayment().getIsAdditionalPaid());
        paymentRepository.save(paymentToUpdate);
    }

}
