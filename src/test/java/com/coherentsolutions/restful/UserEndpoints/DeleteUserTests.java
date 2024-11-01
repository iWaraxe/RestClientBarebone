package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
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
    @Story("Delete Existing User with All Fields")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that an existing user can be deleted successfully when all fields are provided.")
    public void testDeleteUser_AllFieldsProvided() throws IOException {
        logger.info("Running Scenario #1: Delete user with all fields provided");

        UserDto userToDelete = UserDto.builder()
                .name("Alice")
                .sex("Female")
                .email("alice@example.com")
                .age(25)
                .zipCode("10001")
                .build();

        // Start Allure step
        Allure.step("Deleting user: Alice with all provided fields");

        ApiResponse response = userService.deleteUser(userToDelete);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(204, response.getStatusCode(), "Expected status code 204");

        // Verify user is deleted
        Allure.step("Verifying that user Alice is deleted");
        ApiResponse getUsersResponse = userService.getUsers(null);
        assertFalse(getUsersResponse.getResponseBody().contains("Alice"), "User Alice should be deleted");

        // Verify zip code is returned to available list
        Allure.step("Verifying that zip code 10001 is available");
        assertTrue(client.getAvailableZipCodes().contains("10001"), "Zip code 10001 should be available");
    }

    @Test
    @Order(2)
    @Story("Delete Existing User with Required Fields Only")
    @Severity(SeverityLevel.NORMAL)
    @Description("Ensure that an existing user can be deleted successfully when only required fields are provided.")
    public void testDeleteUser_RequiredFieldsOnly() throws IOException {
        logger.info("Running Scenario #2: Delete user with required fields only");

        UserDto userToDelete = UserDto.builder()
                .name("Bob")
                .sex("Male")
                .build();

        // Start Allure step
        Allure.step("Deleting user: Bob with required fields only");

        ApiResponse response = userService.deleteUser(userToDelete);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(204, response.getStatusCode(), "Expected status code 204");

        // Verify user is deleted
        Allure.step("Verifying that user Bob is deleted");
        ApiResponse getUsersResponse = userService.getUsers(null);
        assertFalse(getUsersResponse.getResponseBody().contains("Bob"), "User Bob should be deleted");

        // Verify zip code is returned to available list
        Allure.step("Verifying that zip code 20002 is available");
        assertTrue(client.getAvailableZipCodes().contains("20002"), "Zip code 20002 should be available");
    }

    @Test
    @Order(3)
    @Story("Attempt to Delete User with Missing Required Fields")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to delete a user without providing all required fields to ensure proper error handling.")
    public void testDeleteUser_MissingRequiredFields() throws IOException {
        logger.info("Running Scenario #3: Delete user with missing required fields");

        UserDto userToDelete = UserDto.builder()
                .name("Charlie")
                // Missing 'sex' field
                .build();

        // Start Allure step
        Allure.step("Attempting to delete user: Charlie with missing required fields");

        ApiResponse response = userService.deleteUser(userToDelete);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(409, response.getStatusCode(), "Expected status code 409");

        // Verify error message
        Allure.step("Verifying error message for missing required fields");
        assertTrue(response.getResponseBody().contains("Name and sex are required fields"), "Expected error message about missing required fields");
    }

    @Test
    @Order(4)
    @Story("Delete Non-Existent User")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Attempt to delete a user that does not exist to ensure proper error handling.")
    public void testDeleteUser_NonExistentUser() throws IOException {
        logger.info("Running Scenario #4: Delete a non-existent user");

        UserDto userToDelete = UserDto.builder()
                .name("NonExistentUser")
                .sex("Female")
                .build();

        // Start Allure step
        Allure.step("Attempting to delete non-existent user: NonExistentUser");

        ApiResponse response = userService.deleteUser(userToDelete);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(409, response.getStatusCode(), "Expected status code 409");

        // Verify error message
        Allure.step("Verifying error message for non-existent user");
        assertTrue(response.getResponseBody().contains("User not found"), "Expected error message 'User not found'");
    }

    @Test
    @Order(5)
    @Story("Unauthorized Deletion of User")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Attempt to delete a user with invalid tokens to ensure unauthorized access is handled correctly.")
    public void testDeleteUser_UnauthorizedAccess() throws IOException {
        logger.info("Running Scenario #5: Unauthorized access to delete user");

        // Invalidate tokens to simulate unauthorized access
        Allure.step("Invalidating tokens to simulate unauthorized access");
        client.invalidateTokens();

        UserDto userToDelete = UserDto.builder()
                .name("Alice")
                .sex("Female")
                .build();

        // Start Allure step
        Allure.step("Attempting to delete user: Alice with invalid tokens");

        ApiResponse response = userService.deleteUser(userToDelete);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(401, response.getStatusCode(), "Expected status code 401");

        // Validate the tokens
        Allure.step("Validating tokens after unauthorized attempt");
        client.validateTokens();
    }

    @Test
    @Order(6)
    @Story("Delete User with Invalid Token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Attempt to delete a user using an explicitly invalid token to ensure unauthorized access is handled correctly.")
    public void testDeleteUser_InvalidToken() throws IOException {
        logger.info("Running Scenario #6: Delete user with invalid token");

        // Set invalid token
        Allure.step("Setting an explicitly invalid token");
        client.setInvalidToken();

        UserDto userToDelete = UserDto.builder()
                .name("Bob")
                .sex("Male")
                .build();

        // Start Allure step
        Allure.step("Attempting to delete user: Bob with an invalid token");

        ApiResponse response = userService.deleteUser(userToDelete);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(401, response.getStatusCode(), "Expected status code 401");

        // Validate the tokens
        Allure.step("Validating tokens after invalid token usage");
        client.validateTokens();
    }

    @Test
    @Order(7)
    @Story("Attempt to Use Invalid HTTP Method")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to use an unsupported HTTP method to delete a user and ensure proper error handling.")
    public void testDeleteUser_InvalidMethod() throws IOException {
        logger.info("Running Scenario #7: Invalid HTTP method for delete user");

        // Start Allure step
        Allure.step("Attempting to use DELETE method on a GET-only endpoint");

        // Attempt to use DELETE method on a GET-only endpoint
        ApiResponse response = userService.sendInvalidDeleteMethodRequest();

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(405, response.getStatusCode(), "Expected status code 405 for invalid HTTP method");
    }

    /**
     * Attaches response details to the Allure report.
     *
     * @param response The ApiResponse object containing status code and response body.
     */
    @Attachment(value = "Response Status Code", type = "text/plain")
    public String attachStatusCode(ApiResponse response) {
        return String.valueOf(response.getStatusCode());
    }

    @Attachment(value = "Response Body", type = "text/plain")
    public String attachResponseBody(ApiResponse response) {
        return response.getResponseBody();
    }

    /**
     * Helper method to attach both status code and response body.
     *
     * @param response The ApiResponse object containing status code and response body.
     */
    private void attachResponseDetails(ApiResponse response) {
        attachStatusCode(response);
        attachResponseBody(response);
    }
}
