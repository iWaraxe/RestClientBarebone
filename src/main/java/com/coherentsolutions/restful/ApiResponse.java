package com.coherentsolutions.restful;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse {
    private final int statusCode;
    private final String responseBody;
}
