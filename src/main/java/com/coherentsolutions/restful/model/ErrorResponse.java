package com.coherentsolutions.restful.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final String message;
    private final String errorCode;
    private final int statusCode;
    private final LocalDateTime timestamp;
    private final String path;
    private final Map<String, String> validationErrors;
    private final String requestId;
    private final Long retryAfter;
}
