package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateUserScenarios {

    private UserService userService;
    private OAuth2Client client;
    private UserDto userToChange;

    @BeforeEach
    void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        userService = new UserService(client);

        // Clean up users
        userService.deleteAllUsers();

        // Reset zip codes
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));

        // Create a user to update
        User user = User.builder()
                .name("OriginalUser")
                .email("original@example.com")
                .sex("Male")
                .age(25)
                .zipCode("10001")
                .build();

        ApiResponse response = userService.createUser(user);
        assertEquals(201, response.getStatusCode(), "User creation failed");

        // Set up userToChange
        userToChange = UserDto.builder()
                .name("OriginalUser")
                .sex("Male")
                .build();
    }

    @Test
    public void testUpdateUser_Success() throws IOException {
        UserDto userNewValues = UserDto.builder()
                .name("UpdatedUser")
                .email("updated@example.com")
                .sex("Female")
                .age(28)
                .zipCode("10001")
                .build();

        UpdateUserDto updateUserDto = new UpdateUserDto(userNewValues, userToChange);

        ApiResponse response = userService.updateUser(updateUserDto);
        assertEquals(200, response.getStatusCode(), "Expected status code 200");
    }

    @Test
    public void testUpdateUser_InvalidZipCode() throws IOException {
        UserDto userNewValues = UserDto.builder()
                .email("updated@example.com")
                .age(28)
                .zipCode("99999") // Invalid zip code
                .build();

        UpdateUserDto updateUserDto = new UpdateUserDto(userNewValues, userToChange);

        ApiResponse response = userService.updateUser(updateUserDto);
        assertEquals(424, response.getStatusCode(), "Expected status code 424");
    }


    @Test
    public void testUpdateUser_MissingRequiredFields() throws IOException {
        UserDto userNewValues = UserDto.builder()
                .email("updated@example.com")
                .age(28)
                .build(); // Missing 'name' and 'sex'

        UpdateUserDto updateUserDto = new UpdateUserDto(userNewValues, userToChange);

        ApiResponse response = userService.updateUser(updateUserDto);
        assertEquals(400, response.getStatusCode(), "Expected status code 400");
    }
}
