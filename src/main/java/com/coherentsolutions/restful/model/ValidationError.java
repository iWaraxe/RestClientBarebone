package com.coherentsolutions.restful.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationError {
    private final String field;
    private final String message;
    private final String code;
    private final Object rejectedValue;
}