package org.ks.photoapp.domain.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.photosession.PhotoSession;
import org.ks.photoapp.domain.photosession.PhotoSessionRepository;
import org.ks.photoapp.domain.payment.exception.PaymentNotFoundException;
import org.ks.photoapp.domain.photosession.exception.PhotoSessionNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    PaymentRepository paymentRepository;
    PhotoSessionRepository photoSessionRepository;
    PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        photoSessionRepository = mock(PhotoSessionRepository.class);
        paymentService = new PaymentService(paymentRepository, photoSessionRepository);
    }

    @Test
    void updatePayment_updatesPaymentFromPhotoSession() {
        long paymentId = 1L;
        Client client = new Client(); client.setId(5L);

        Payment existing = new Payment();
        existing.setId(paymentId);

        Payment source = new Payment();
        source.setDeposit(100f);
        source.setBasePayment(200f);
        source.setAdditionalPayment(50f);
        source.setIsDepositPaid(true);
        source.setIsBasePaid(false);
        source.setIsAdditionalPaid(true);

        PhotoSession ps = new PhotoSession();
        ps.setPayment(source);

        when(paymentRepository.findPaymentById(paymentId)).thenReturn(Optional.of(existing));
        when(photoSessionRepository.findPhotoSessionByClient(client)).thenReturn(Optional.of(ps));

        paymentService.updatePayment(paymentId, client);

        verify(paymentRepository).save(existing);
    }

    @Test
    void updatePayment_throwsWhenPaymentMissing() {
        long paymentId = 2L;
        Client client = new Client();

        when(paymentRepository.findPaymentById(paymentId)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.updatePayment(paymentId, client));
    }

    @Test
    void updatePayment_throwsWhenPhotoSessionMissing() {
        long paymentId = 3L;
        Client client = new Client();
        Payment existing = new Payment(); existing.setId(paymentId);

        when(paymentRepository.findPaymentById(paymentId)).thenReturn(Optional.of(existing));
        when(photoSessionRepository.findPhotoSessionByClient(client)).thenReturn(Optional.empty());

        assertThrows(PhotoSessionNotFoundException.class, () -> paymentService.updatePayment(paymentId, client));
    }
}
