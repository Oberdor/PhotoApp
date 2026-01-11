package org.ks.photoapp.domain.payment;

import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.client.ClientRepository;
import org.ks.photoapp.domain.payment.dto.PaymentDto;
import org.ks.photoapp.domain.payment.dto.PaymentUpdateRequest;
import org.ks.photoapp.domain.photoSession.PhotoSession;
import org.ks.photoapp.domain.photoSession.PhotoSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public Payment updatePaymentStatus(Long paymentId, PaymentUpdateRequest request) {
        Payment payment = paymentRepository.findPaymentById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean changed = false;
        if (request.getIsDepositPaid() != null && !request.getIsDepositPaid().equals(payment.getIsDepositPaid())) {
            payment.setIsDepositPaid(request.getIsDepositPaid());
            changed = true;
        }
        if (request.getIsBasePaid() != null && !request.getIsBasePaid().equals(payment.getIsBasePaid())) {
            payment.setIsBasePaid(request.getIsBasePaid());
            changed = true;
        }
        if (request.getIsAdditionalPaid() != null && !request.getIsAdditionalPaid().equals(payment.getIsAdditionalPaid())) {
            payment.setIsAdditionalPaid(request.getIsAdditionalPaid());
            changed = true;
        }

        if (changed) {
            payment = paymentRepository.save(payment);
        }

        PhotoSession ps = payment.getPhotoSession();
        if (ps != null) {
            boolean depositPaid = Boolean.TRUE.equals(payment.getIsDepositPaid());
            boolean basePaid = Boolean.TRUE.equals(payment.getIsBasePaid());
            // assume deposit + base are required to finish contract
            if (depositPaid && basePaid && !Boolean.TRUE.equals(ps.getIsContractFinished())) {
                ps.setIsContractFinished(true);
                photoSessionRepository.save(ps);
            }
        }

        return payment;
    }

}
