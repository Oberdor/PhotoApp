package org.ks.photoapp.domain.payment.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ks.photoapp.domain.payment.Payment;
import org.ks.photoapp.domain.payment.PaymentRepository;
import org.ks.photoapp.domain.photoSession.PhotoSession;
import org.ks.photoapp.domain.photoSession.PhotoSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PhotoSessionRepository photoSessionRepository;

    @BeforeEach
    void cleanup() {
        photoSessionRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    void patch_partial_updates_deposit_flag() throws Exception {
        Payment payment = new Payment();
        payment.setDeposit(50f);
        payment.setBasePayment(100f);
        payment.setAdditionalPayment(20f);
        payment.setIsDepositPaid(false);
        payment.setIsBasePaid(false);
        payment.setIsAdditionalPaid(false);
        paymentRepository.save(payment);

        PhotoSession session = new PhotoSession();
        session.setPayment(payment);
        session.setIsContractFinished(false);
        photoSessionRepository.save(session);

        // link back
        payment.setPhotoSession(session);
        paymentRepository.save(payment);

        String body = "{\"isDepositPaid\": true}";

        mockMvc.perform(patch("/api/payments/" + payment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDepositPaid").value(true))
                .andExpect(jsonPath("$.isBasePaid").value(false));
    }

    @Test
    void patch_all_flags_closes_contract() throws Exception {
        Payment payment = new Payment();
        payment.setDeposit(10f);
        payment.setBasePayment(20f);
        payment.setAdditionalPayment(5f);
        payment.setIsDepositPaid(false);
        payment.setIsBasePaid(false);
        payment.setIsAdditionalPaid(false);
        paymentRepository.save(payment);

        PhotoSession session = new PhotoSession();
        session.setPayment(payment);
        session.setIsContractFinished(false);
        photoSessionRepository.save(session);

        payment.setPhotoSession(session);
        paymentRepository.save(payment);

        String body = objectMapper.writeValueAsString(new Object() {
            public final Boolean isDepositPaid = true;
            public final Boolean isBasePaid = true;
            public final Boolean isAdditionalPaid = true;
        });

        mockMvc.perform(patch("/api/payments/" + payment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isContractFinished").value(true));

        PhotoSession updated = photoSessionRepository.findPhotoSessionById(session.getId()).orElseThrow();
        assertThat(Boolean.TRUE).isEqualTo(updated.getIsContractFinished());
    }

    @Test
    void patch_empty_body_returns_400() throws Exception {
        mockMvc.perform(patch("/api/payments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patch_nonexistent_payment_returns_404() throws Exception {
        String body = "{\"isDepositPaid\": true}";
        mockMvc.perform(patch("/api/payments/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void patch_on_finished_contract_returns_409() throws Exception {
        Payment payment = new Payment();
        payment.setIsDepositPaid(false);
        payment.setIsBasePaid(false);
        payment.setIsAdditionalPaid(false);
        paymentRepository.save(payment);

        PhotoSession session = new PhotoSession();
        session.setPayment(payment);
        session.setIsContractFinished(true);
        photoSessionRepository.save(session);

        payment.setPhotoSession(session);
        paymentRepository.save(payment);

        String body = "{\"isDepositPaid\": true}";
        mockMvc.perform(patch("/api/payments/" + payment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

}
