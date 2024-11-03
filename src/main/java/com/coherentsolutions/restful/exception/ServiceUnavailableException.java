package com.coherentsolutions.restful.exception;

// Service unavailable exceptions
public class ServiceUnavailableException extends ApiException {
    public ServiceUnavailableException(String message) {
        super(message, 503, "SERVICE_UNAVAILABLE");
    }
}
