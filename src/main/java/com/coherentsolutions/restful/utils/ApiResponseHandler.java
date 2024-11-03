package com.coherentsolutions.restful.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class ApiResponseHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T parseResponse(String responseBody, Class<T> responseType) throws IOException {
        return objectMapper.readValue(responseBody, responseType);
    }
}