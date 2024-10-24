package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.ApiResponse;
import com.coherentsolutions.restful.OAuth2Client;
import com.coherentsolutions.restful.User;
import com.coherentsolutions.restful.UserService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateUserClassic {

    private static UserService userService;
    private static OAuth2Client client;
    // Astatic variable to hold the user ID
    private static Long testUserId;

    @BeforeAll
    static void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        userService = new UserService(client);

        // Reset zip codes to a known state
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));

        // Create a user to update
        User user = User.builder()
                .name("TestUser")
                .email("testuser@example.com")
                .sex("Male")
                .age(25)
                .zipCode("10001")
                .build();

        ApiResponse response = userService.createUser(user);
        assertEquals(201, response.getStatusCode(), "User creation failed");

        // Parse response body to get user ID
        JSONObject jsonResponse = new JSONObject(response.getResponseBody());
        testUserId = jsonResponse.getLong("id");
    }

    @Test
    public void testUpdateUser_Success() throws IOException {
        User user = User.builder()
                .name("UpdatedName")
                .email("updated@example.com")
                .sex("Female")
                .age(28)
                .zipCode("10001")
                .build();

        ApiResponse response = userService.updateUser(testUserId, user);
        assertEquals(200, response.getStatusCode(), "Expected status code 200");
    }


    @Test
    public void testUpdateUser_InvalidZipCode() throws IOException {
        User user = User.builder()
                .name("UpdatedName")
                .email("updated@example.com")
                .sex("Female")
                .age(28)
                .zipCode("99999") // Invalid zip code
                .build();

        ApiResponse response = userService.updateUser(testUserId, user);
        assertEquals(424, response.getStatusCode(), "Expected status code 424");
    }

    @Test
    public void testUpdateUser_MissingRequiredFields() throws IOException {
        User user = User.builder()
                .email("updated@example.com")
                .age(28)
                .build(); // Missing 'name' and 'sex'

        ApiResponse response = userService.updateUser(testUserId, user);
        assertEquals(409, response.getStatusCode(), "Expected status code 409");
    }

}
