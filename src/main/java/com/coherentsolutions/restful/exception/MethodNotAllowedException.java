package com.coherentsolutions.restful.exception;

public class MethodNotAllowedException extends ApiException {
    public MethodNotAllowedException(String message) {
        super(message, 405, "METHOD_NOT_ALLOWED");
    }
}
