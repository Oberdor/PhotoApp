package org.ks.photoapp.domain.payment;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/update-paid")
    public String updatePaid(@RequestParam long paymentId, @RequestParam long clientId) {
        paymentService.updatePayment(paymentId, clientId);
        return "redirect:/payments";
    }

}

