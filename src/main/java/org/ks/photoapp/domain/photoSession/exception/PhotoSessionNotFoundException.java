package org.ks.photoapp.domain.photosession.exception;

public class PhotoSessionNotFoundException extends RuntimeException {
    public PhotoSessionNotFoundException(String message) {
        super(message);
    }

    public PhotoSessionNotFoundException(Long id) {
        super("PhotoSession not found with id: " + id);
    }
}
