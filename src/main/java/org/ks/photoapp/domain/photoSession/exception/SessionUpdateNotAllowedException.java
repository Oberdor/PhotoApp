package org.ks.photoapp.domain.photosession.exception;

public class SessionUpdateNotAllowedException extends RuntimeException {
    public SessionUpdateNotAllowedException(String message) {
        super(message);
    }

    public SessionUpdateNotAllowedException() {
        super("Session update not allowed");
    }
}
