package com.coherentsolutions.restful.exception;

// Authorization related exceptions
public class AuthorizationException extends ApiException {
    public AuthorizationException(String message) {
        super(message, 403, "FORBIDDEN");
    }
}