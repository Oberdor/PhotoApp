package org.ks.photoapp.domain.payment.api;

import jakarta.servlet.http.HttpServletRequest;
import org.ks.photoapp.domain.payment.ContractAlreadyFinishedException;
import org.ks.photoapp.domain.payment.InvalidPaymentPatchRequestException;
import org.ks.photoapp.domain.payment.PaymentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@ControllerAdvice
public class PaymentErrorHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(PaymentNotFoundException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                404,
                "Not Found",
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidPaymentPatchRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(InvalidPaymentPatchRequestException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                400,
                "Bad Request",
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ContractAlreadyFinishedException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ContractAlreadyFinishedException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                409,
                "Conflict",
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                400,
                "Bad Request",
                "Malformed JSON: " + ex.getMostSpecificCause().getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                400,
                "Bad Request",
                "Invalid argument: " + ex.getName(),
                Instant.now().toString(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

}
