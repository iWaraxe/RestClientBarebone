package com.coherentsolutions.restful.exception;

// Authentication related exceptions
public class AuthenticationException extends ApiException {
    public AuthenticationException(String message) {
        super(message, 401, "AUTHENTICATION_ERROR");
    }
}