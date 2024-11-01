package com.coherentsolutions.restful.user;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserDto {
    private UserDto userNewValues;
    private UserDto userToChange;
}
