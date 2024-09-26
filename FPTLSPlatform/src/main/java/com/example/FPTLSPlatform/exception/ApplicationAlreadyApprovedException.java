package com.example.FPTLSPlatform.exception;

public class ApplicationAlreadyApprovedException extends RuntimeException {
    public ApplicationAlreadyApprovedException(String message) {
        super(message);
    }
}
