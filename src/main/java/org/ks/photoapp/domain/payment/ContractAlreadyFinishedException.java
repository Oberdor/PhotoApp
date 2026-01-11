package org.ks.photoapp.domain.payment;

public class ContractAlreadyFinishedException extends RuntimeException {
    public ContractAlreadyFinishedException(String message) {
        super(message);
    }
}
