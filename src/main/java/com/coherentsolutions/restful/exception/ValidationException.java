package com.coherentsolutions.restful.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

// Invalid input exceptions
@Getter
public class ValidationException extends ApiException {
    private final Map<String, String> validationErrors;

    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message, 400, "VALIDATION_ERROR");
        this.validationErrors = validationErrors != null ? validationErrors : Collections.emptyMap();
    }
}