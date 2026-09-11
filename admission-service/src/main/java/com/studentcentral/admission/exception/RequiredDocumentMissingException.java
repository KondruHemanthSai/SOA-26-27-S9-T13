package com.studentcentral.admission.exception;

public class RequiredDocumentMissingException extends RuntimeException {
    public RequiredDocumentMissingException(String message) {
        super(message);
    }
}
