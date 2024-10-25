package com.coherentsolutions.restful;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String name;
    private String email;
    private String sex;
    private Integer age;
    private String zipCode;
}
