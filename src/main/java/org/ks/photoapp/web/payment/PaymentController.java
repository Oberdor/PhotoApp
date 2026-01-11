package org.ks.photoapp.web.payment;

import org.ks.photoapp.domain.client.Client;
import org.ks.photoapp.domain.payment.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {
    PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/update-paid")
    public String updatePaid(long paymentId, Client client) {
        paymentService.updatePayment(paymentId,client);
        return "redirect:/payments";
    }

}
