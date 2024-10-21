package com.coherentsolutions.restful;

import lombok.*;

@Getter
@AllArgsConstructor
public class ApiResponse {
    private final int statusCode;
    private final String responseBody;
}
