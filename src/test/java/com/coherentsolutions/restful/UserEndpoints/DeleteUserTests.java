package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import com.coherentsolutions.restful.auth.AuthenticationStrategy;
import com.coherentsolutions.restful.auth.BearerTokenAuthentication;
import com.coherentsolutions.restful.auth.OAuth2Client;
import com.coherentsolutions.restful.exception.*;
import com.coherentsolutions.restful.user.User;
import com.coherentsolutions.restful.user.UserDto;
import com.coherentsolutions.restful.user.UserService;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Delete User")
public class DeleteUserTests {

    private static final Logger logger = LoggerFactory.getLogger(DeleteUserTests.class);
    private UserService userService;
    private OAuth2Client client;
    private AuthenticationStrategy writeAuthStrategy;

    @BeforeEach
    @Step("Setting up the test environment")
    void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        client.validateTokens(); // Ensure tokens are valid at start
        writeAuthStrategy = new BearerTokenAuthentication(client);
        userService = new UserService(writeAuthStrategy);

        // Reset zip codes
        Allure.step("Resetting zip codes to default");
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));

        // Clean up users
        Allure.step("Cleaning up existing users");
        userService.deleteAllUsers();

        // Create users for testing
        Allure.step("Creating test users: Alice and Bob");
        createTestUsers();
    }

    private void createTestUsers() {
        try {
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
        } catch (Exception e) {
            logger.error("Failed to create test users", e);
            throw new RuntimeException("Test setup failed", e);
        }
    }

    @Test
    @Order(1)
    @Story("Delete Existing User with All Fields")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that an existing user can be deleted successfully when all fields are provided.")
    public void testDeleteUser_AllFieldsProvided() {
        logger.info("Running Scenario #1: Delete user with all fields provided");

        UserDto userToDelete = UserDto.builder()
                .name("Alice")
                .sex("Female")
                .email("alice@example.com")
                .age(25)
                .zipCode("10001")
                .build();

        Allure.step("Deleting user: Alice with all provided fields");
        assertDoesNotThrow(() -> {
            ApiResponse response = userService.deleteUser(userToDelete);
            attachResponseDetails(response);
            assertEquals(204, response.getStatusCode(), "Expected status code 204");
        });

        // Verify user is deleted
        Allure.step("Verifying that user Alice is deleted");
        assertDoesNotThrow(() -> {
            ApiResponse getUsersResponse = userService.getUsers(null);
            assertFalse(getUsersResponse.getResponseBody().contains("Alice"), "User Alice should be deleted");
        });

        // Verify zip code is returned to available list
        Allure.step("Verifying that zip code 10001 is available");
        assertDoesNotThrow(() -> {
            assertTrue(client.getAvailableZipCodes().contains("10001"), "Zip code 10001 should be available");
        });
    }

    @Test
    @Order(2)
    @Story("Delete Existing User with Required Fields Only")
    @Severity(SeverityLevel.NORMAL)
    @Description("Ensure that an existing user can be deleted successfully when only required fields are provided.")
    public void testDeleteUser_RequiredFieldsOnly() {
        logger.info("Running Scenario #2: Delete user with required fields only");

        UserDto userToDelete = UserDto.builder()
                .name("Bob")
                .sex("Male")
                .build();

        Allure.step("Deleting user: Bob with required fields only");
        assertDoesNotThrow(() -> {
            ApiResponse response = userService.deleteUser(userToDelete);
            attachResponseDetails(response);
            assertEquals(204, response.getStatusCode(), "Expected status code 204");
        });

        // Verify deletion
        verifyUserDeletion("Bob", "20002");
    }

    @Test
    @Order(3)
    @Story("Attempt to Delete User with Missing Required Fields")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to delete a user without providing all required fields to ensure proper error handling.")
    public void testDeleteUser_MissingRequiredFields() {
        logger.info("Running Scenario #3: Delete user with missing required fields");

        UserDto userToDelete = UserDto.builder()
                .name("Charlie")
                .build();

        Allure.step("Attempting to delete user: Charlie with missing required fields");
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.deleteUser(userToDelete);
        });

        assertEquals("Name and sex are required fields", exception.getMessage());
        assertTrue(exception.getValidationErrors().containsKey("sex"));
    }

    @Test
    @Order(4)
    @Story("Delete Non-Existent User")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Attempt to delete a user that does not exist to ensure proper error handling.")
    public void testDeleteUser_NonExistentUser() {
        logger.info("Running Scenario #4: Delete a non-existent user");

        UserDto userToDelete = UserDto.builder()
                .name("NonExistentUser")
                .sex("Female")
                .build();

        Allure.step("Attempting to delete non-existent user: NonExistentUser");
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(userToDelete);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @Order(5)
    @Story("Unauthorized Deletion of User")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Attempt to delete a user with invalid tokens to ensure unauthorized access is handled correctly.")
    public void testDeleteUser_UnauthorizedAccess() {
        logger.info("Running Scenario #5: Unauthorized access to delete user");

        Allure.step("Invalidating tokens to simulate unauthorized access");
        client.invalidateTokens();

        UserDto userToDelete = UserDto.builder()
                .name("Alice")
                .sex("Female")
                .build();

        Allure.step("Attempting to delete user: Alice with invalid tokens");
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.deleteUser(userToDelete);
        });

        assertEquals("Authentication failed", exception.getMessage());

        // Restore valid state
        client.validateTokens();
    }

    @Test
    @Order(6)
    @Story("Delete User with Invalid Token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Attempt to delete a user using an explicitly invalid token to ensure unauthorized access is handled correctly.")
    public void testDeleteUser_InvalidToken() {
        logger.info("Running Scenario #6: Delete user with invalid token");

        Allure.step("Setting an explicitly invalid token");
        client.setInvalidToken();

        UserDto userToDelete = UserDto.builder()
                .name("Bob")
                .sex("Male")
                .build();

        Allure.step("Attempting to delete user: Bob with an invalid token");
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.deleteUser(userToDelete);
        });

        assertEquals("Authentication failed", exception.getMessage());

        // Restore valid state
        client.validateTokens();
    }

    @Test
    @Order(7)
    @Story("Attempt to Use Invalid HTTP Method")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to use an unsupported HTTP method to delete a user and ensure proper error handling.")
    public void testDeleteUser_InvalidMethod() {
        logger.info("Running Scenario #7: Invalid HTTP method for delete user");

        Allure.step("Attempting to use DELETE method on a GET-only endpoint");
        ApiException exception = assertThrows(ApiException.class, () -> {
            userService.sendInvalidDeleteMethodRequest();
        });

        assertEquals(405, exception.getStatusCode());
        assertEquals("METHOD_NOT_ALLOWED", exception.getErrorCode());
    }

    private void verifyUserDeletion(String userName, String zipCode) {
        assertDoesNotThrow(() -> {
            ApiResponse getUsersResponse = userService.getUsers(null);
            assertFalse(getUsersResponse.getResponseBody().contains(userName),
                    "User " + userName + " should be deleted");

            assertTrue(client.getAvailableZipCodes().contains(zipCode),
                    "Zip code " + zipCode + " should be available");
        });
    }

    @Attachment(value = "Response Status Code", type = "text/plain")
    public String attachStatusCode(ApiResponse response) {
        return String.valueOf(response.getStatusCode());
    }

    @Attachment(value = "Response Body", type = "text/plain")
    public String attachResponseBody(ApiResponse response) {
        return response.getResponseBody();
    }

    private void attachResponseDetails(ApiResponse response) {
        attachStatusCode(response);
        attachResponseBody(response);
    }
}