package com.onlinepharmacy.exception;

import org.springframework.http.HttpStatus;

public class PrescriptionRequiredException extends ApiException {

    public PrescriptionRequiredException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}