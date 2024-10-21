package com.coherentsolutions.restful;

import lombok.*;

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
