package org.ks.photoapp.domain.payment;

import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.payment.exception.PaymentNotFoundException;
import org.ks.photoapp.domain.photosession.PhotoSession;
import org.ks.photoapp.domain.photosession.PhotoSessionRepository;
import org.ks.photoapp.domain.photosession.exception.PhotoSessionNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PhotoSessionRepository photoSessionRepository;

    public PaymentService(PaymentRepository paymentRepository, PhotoSessionRepository photoSessionRepository) {
        this.paymentRepository = paymentRepository;
        this.photoSessionRepository = photoSessionRepository;
    }

    public void updatePayment(long paymentId, Client client) {
        Payment paymentToUpdate = paymentRepository.findPaymentById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        PhotoSession photoSession = photoSessionRepository.findPhotoSessionByClient(client)
                .orElseThrow(() -> new PhotoSessionNotFoundException("No PhotoSession for client id: " + (client != null ? client.getId() : "null")));
        paymentToUpdate.setDeposit(photoSession.getPayment().getDeposit());
        paymentToUpdate.setBasePayment(photoSession.getPayment().getBasePayment());
        paymentToUpdate.setAdditionalPayment(photoSession.getPayment().getAdditionalPayment());
        paymentToUpdate.setIsDepositPaid(photoSession.getPayment().getIsDepositPaid());
        paymentToUpdate.setIsBasePaid(photoSession.getPayment().getIsBasePaid());
        paymentToUpdate.setIsAdditionalPaid(photoSession.getPayment().getIsAdditionalPaid());
        paymentRepository.save(paymentToUpdate);
    }

}
