package org.ks.photoapp.domain.payment.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ks.photoapp.domain.payment.PaymentService;
import org.ks.photoapp.domain.payment.api.dto.PaymentPatchRequest;
import org.ks.photoapp.domain.payment.api.dto.PaymentDto;
import org.ks.photoapp.domain.payment.InvalidPaymentPatchRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentRestController(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @PatchMapping("/{paymentId}")
    public ResponseEntity<PaymentDto> patchPayment(@PathVariable Long paymentId,
                                                   @RequestBody JsonNode requestBody) {
        if (requestBody == null || !requestBody.isObject()) {
            throw new InvalidPaymentPatchRequestException("Request body must be a JSON object");
        }

        if (requestBody.size() == 0) {
            throw new InvalidPaymentPatchRequestException("Request body must contain at least one updatable field");
        }

        // allowed fields
        String[] allowed = {"isDepositPaid", "isBasePaid", "isAdditionalPaid"};

        for (var it = requestBody.fieldNames(); it.hasNext(); ) {
            String field = it.next();
            boolean ok = false;
            for (String a : allowed) if (a.equals(field)) { ok = true; break; }
            if (!ok) {
                throw new InvalidPaymentPatchRequestException("Unexpected field '" + field + "'");
            }
            JsonNode val = requestBody.get(field);
            if (val == null || val.isNull()) {
                throw new InvalidPaymentPatchRequestException("Field '" + field + "' must not be null");
            }
            if (!val.isBoolean()) {
                throw new InvalidPaymentPatchRequestException("Field '" + field + "' must be boolean");
            }
        }

        PaymentPatchRequest request = objectMapper.convertValue(requestBody, PaymentPatchRequest.class);

        PaymentDto result = paymentService.patchPayment(paymentId, request);
        return ResponseEntity.ok(result);
    }
}
