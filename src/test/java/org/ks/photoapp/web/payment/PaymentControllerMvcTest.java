package org.ks.photoapp.web.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.ks.photoapp.web.payment.PaymentController;
import org.ks.photoapp.domain.payment.PaymentService;
import org.ks.photoapp.domain.client.Client;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class)
class PaymentControllerMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PaymentService paymentService;

    @Test
    void updatePaid_redirectsToPayments() throws Exception {
        mockMvc.perform(get("/update-paid").param("paymentId", "3").param("client", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments"));

        verify(paymentService).updatePayment(eq(3L), any(Client.class));
    }
}
