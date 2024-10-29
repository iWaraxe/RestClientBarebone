package com.coherentsolutions.restful;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadUserDto {
    private String name;
    private String email;
    private String sex;
    private ZipCodeDto zipCode;
    private int age;
}