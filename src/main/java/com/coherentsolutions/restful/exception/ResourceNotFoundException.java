package com.coherentsolutions.restful.exception;

// Resource not found exceptions
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(message, 404, "RESOURCE_NOT_FOUND");
    }
}