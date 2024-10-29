package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeleteUserTests {

    private static final Logger logger = LoggerFactory.getLogger(DeleteUserTests.class);
    private UserService userService;
    private OAuth2Client client;

    @BeforeEach
    void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        userService = new UserService(client);

        // Reset zip codes
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));

        // Clean up users
        userService.deleteAllUsers();

        // Create users for testing
        userService.createUser(User.builder()
                .name("Alice")
                .email("alice@example.com")
                .sex("Female")
                .age(25)
                .zipCode("10001")
                .build());

        userService.createUser(User.builder()
                .name("Bob")
                .email("bob@example.com")
                .sex("Male")
                .age(30)
                .zipCode("20002")
                .build());
    }

    @Test
    @Order(1)
    public void testDeleteUser_AllFieldsProvided() throws IOException {
        logger.info("Running Scenario #1: Delete user with all fields provided");

        UserDto userToDelete = UserDto.builder()
                .name("Alice")
                .sex("Female")
                .email("alice@example.com")
                .age(25)
                .zipCode("10001")
                .build();

        ApiResponse response = userService.deleteUser(userToDelete);
        assertEquals(204, response.getStatusCode(), "Expected status code 204");

        // Verify user is deleted
        ApiResponse getUsersResponse = userService.getUsers(null);
        assertFalse(getUsersResponse.getResponseBody().contains("Alice"), "User Alice should be deleted");

        // Verify zip code is returned to available list
        assertTrue(client.getAvailableZipCodes().contains("10001"), "Zip code 10001 should be available");
    }

    @Test
    @Order(2)
    public void testDeleteUser_RequiredFieldsOnly() throws IOException {
        logger.info("Running Scenario #2: Delete user with required fields only");

        UserDto userToDelete = UserDto.builder()
                .name("Bob")
                .sex("Male")
                .build();

        ApiResponse response = userService.deleteUser(userToDelete);
        assertEquals(204, response.getStatusCode(), "Expected status code 204");

        // Verify user is deleted
        ApiResponse getUsersResponse = userService.getUsers(null);
        assertFalse(getUsersResponse.getResponseBody().contains("Bob"), "User Bob should be deleted");

        // Verify zip code is returned to available list
        assertTrue(client.getAvailableZipCodes().contains("20002"), "Zip code 20002 should be available");
    }

    @Test
    @Order(3)
    public void testDeleteUser_MissingRequiredFields() throws IOException {
        logger.info("Running Scenario #3: Delete user with missing required fields");

        UserDto userToDelete = UserDto.builder()
                .name("Charlie")
                // Missing 'sex' field
                .build();

        ApiResponse response = userService.deleteUser(userToDelete);
        assertEquals(409, response.getStatusCode(), "Expected status code 409");

        // Verify error message
        assertTrue(response.getResponseBody().contains("Name and sex are required fields"), "Expected error message about missing required fields");
    }

    @Test
    @Order(4)
    public void testDeleteUser_NonExistentUser() throws IOException {
        logger.info("Running Scenario: Delete a non-existent user");

        UserDto userToDelete = UserDto.builder()
                .name("NonExistentUser")
                .sex("Female")
                .build();

        ApiResponse response = userService.deleteUser(userToDelete);
        assertEquals(409, response.getStatusCode(), "Expected status code 409");

        // Verify error message
        assertTrue(response.getResponseBody().contains("User not found"), "Expected error message 'User not found'");
    }

    @Test
    @Order(5)
    public void testDeleteUser_UnauthorizedAccess() throws IOException {
        logger.info("Running Scenario: Unauthorized access to delete user");

        // Invalidate tokens to simulate unauthorized access
        client.invalidateTokens();

        UserDto userToDelete = UserDto.builder()
                .name("Alice")
                .sex("Female")
                .build();

        ApiResponse response = userService.deleteUser(userToDelete);
        assertEquals(401, response.getStatusCode(), "Expected status code 401");

        // Validate the tokens
        client.validateTokens();
    }

    @Test
    @Order(6)
    public void testDeleteUser_InvalidToken() throws IOException {
        logger.info("Running Scenario: Delete user with invalid token");

        // Set invalid token
        client.setInvalidToken();

        UserDto userToDelete = UserDto.builder()
                .name("Bob")
                .sex("Male")
                .build();

        ApiResponse response = userService.deleteUser(userToDelete);
        assertEquals(401, response.getStatusCode(), "Expected status code 401");

        // Validate the tokens
        client.validateTokens();
    }

    @Test
    @Order(7)
    public void testDeleteUser_InvalidMethod() throws IOException {
        logger.info("Running Scenario: Invalid HTTP method for delete user");

        // Attempt to use DELETE method on a GET-only endpoint
        ApiResponse response = userService.sendInvalidDeleteMethodRequest();
        assertEquals(405, response.getStatusCode(), "Expected status code 405 for invalid HTTP method");
    }
}
