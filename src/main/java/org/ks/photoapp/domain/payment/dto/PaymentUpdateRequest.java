package org.ks.photoapp.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentUpdateRequest {
    Boolean isDepositPaid;
    Boolean isBasePaid;
    Boolean isAdditionalPaid;
}
