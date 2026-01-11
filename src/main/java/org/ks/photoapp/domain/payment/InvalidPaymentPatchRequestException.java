package org.ks.photoapp.domain.payment;

public class InvalidPaymentPatchRequestException extends RuntimeException {
    public InvalidPaymentPatchRequestException(String message) {
        super(message);
    }
}
