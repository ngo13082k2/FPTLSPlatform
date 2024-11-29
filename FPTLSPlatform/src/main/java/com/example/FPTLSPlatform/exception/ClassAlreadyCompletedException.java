package com.example.FPTLSPlatform.exception;

public class ClassAlreadyCompletedException extends RuntimeException {
    public ClassAlreadyCompletedException(String message) {
        super(message);
    }
}
