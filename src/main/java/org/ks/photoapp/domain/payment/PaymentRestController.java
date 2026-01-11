package org.ks.photoapp.domain.payment;

import org.ks.photoapp.domain.payment.dto.PaymentDto;
import org.ks.photoapp.domain.payment.dto.PaymentUpdateRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {
    private final PaymentService paymentService;

    public PaymentRestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PatchMapping("/{paymentId}")
    public PaymentDto updatePayment(@PathVariable Long paymentId, @RequestBody PaymentUpdateRequest request) {
        Payment updated = paymentService.updatePaymentStatus(paymentId, request);
        return PaymentDtoMapper.map(updated);
    }
}
