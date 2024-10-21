package com.coherentsolutions.restful;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String name;
    private String email;
    private String sex;
    private String zipCode; // Keep as String to send the zip code code
}
