package com.coherentsolutions.restful.exception;

// Resource conflict exceptions
public class ResourceConflictException extends ApiException {
    public ResourceConflictException(String message) {
        super(message, 409, "RESOURCE_CONFLICT");
    }
}