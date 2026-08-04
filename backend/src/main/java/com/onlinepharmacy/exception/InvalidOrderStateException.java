package com.onlinepharmacy.exception;

import org.springframework.http.HttpStatus;

public class InvalidOrderStateException extends ApiException {

    public InvalidOrderStateException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}