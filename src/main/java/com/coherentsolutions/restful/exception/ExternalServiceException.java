package com.coherentsolutions.restful.exception;

// External service exceptions
public class ExternalServiceException extends ApiException {
    public ExternalServiceException(String message) {
        super(message, 424, "EXTERNAL_SERVICE_ERROR");
    }
}