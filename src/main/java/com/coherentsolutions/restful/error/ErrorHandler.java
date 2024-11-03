package com.coherentsolutions.restful.error;

import com.coherentsolutions.restful.ApiResponse;
import com.coherentsolutions.restful.exception.*;
import com.coherentsolutions.restful.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.core5.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    private final ObjectMapper objectMapper;

    public ErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ApiResponse handleException(Exception ex, String path) {
        String requestId = MDC.get("requestId");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);
        }

        ErrorResponse.ErrorResponseBuilder errorBuilder = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .path(path)
                .requestId(requestId);

        ApiResponse response;

        try {
            if (ex instanceof ApiException) {
                response = handleApiException((ApiException) ex, errorBuilder);
            } else if (ex instanceof IOException) {
                response = handleIOException((IOException) ex, errorBuilder);
            } else if (ex instanceof HttpException) {
                response = handleHttpException((HttpException) ex, errorBuilder);
            } else {
                response = handleUnexpectedException(ex, errorBuilder);
            }

            logger.error("Request {} failed: {}", requestId, response.getResponseBody(), ex);
            return response;
        } finally {
            MDC.remove("requestId");
        }
    }

    private ApiResponse handleApiException(ApiException ex, ErrorResponse.ErrorResponseBuilder errorBuilder) {
        ErrorResponse errorResponse = errorBuilder
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .statusCode(ex.getStatusCode())
                .build();

        if (ex instanceof ValidationException) {
            errorResponse = errorBuilder
                    .validationErrors(((ValidationException) ex).getValidationErrors())
                    .build();
        } else if (ex instanceof RateLimitException) {
            errorResponse = errorBuilder
                    .retryAfter(((RateLimitException) ex).getRetryAfterSeconds())
                    .build();
        }

        return new ApiResponse(ex.getStatusCode(), serializeError(errorResponse));
    }

    private ApiResponse handleIOException(IOException ex, ErrorResponse.ErrorResponseBuilder errorBuilder) {
        ErrorResponse errorResponse = errorBuilder
                .message("Communication error occurred")
                .errorCode("COMMUNICATION_ERROR")
                .statusCode(503)
                .build();

        return new ApiResponse(503, serializeError(errorResponse));
    }

    private ApiResponse handleHttpException(HttpException ex, ErrorResponse.ErrorResponseBuilder errorBuilder) {
        ErrorResponse errorResponse = errorBuilder
                .message("HTTP protocol error occurred")
                .errorCode("HTTP_ERROR")
                .statusCode(500)
                .build();

        return new ApiResponse(500, serializeError(errorResponse));
    }

    private ApiResponse handleUnexpectedException(Exception ex, ErrorResponse.ErrorResponseBuilder errorBuilder) {
        ErrorResponse errorResponse = errorBuilder
                .message("An unexpected error occurred")
                .errorCode("INTERNAL_ERROR")
                .statusCode(500)
                .build();

        return new ApiResponse(500, serializeError(errorResponse));
    }

    private String serializeError(ErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsString(errorResponse);
        } catch (Exception e) {
            logger.error("Error serializing error response", e);
            return "{\"message\":\"Error processing error response\",\"statusCode\":500}";
        }
    }
}