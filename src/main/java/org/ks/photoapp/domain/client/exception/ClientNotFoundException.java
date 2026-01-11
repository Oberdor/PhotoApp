package org.ks.photoapp.domain.client.exception;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(String message) {
        super(message);
    }

    public ClientNotFoundException(Long id) {
        super("Client not found with id: " + id);
    }
}
