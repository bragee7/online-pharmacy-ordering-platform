package com.onlinepharmacy.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base class for all business exceptions thrown by the application.
 * Carries a {@link HttpStatus} used by the global exception handler.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}