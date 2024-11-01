package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import io.qameta.allure.*;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Update User")
public class UpdateUserClassicsTests {

    private static final Logger logger = LoggerFactory.getLogger(UpdateUserClassicsTests.class);
    private static UserService userService;
    private static OAuth2Client client;
    // A static variable to hold the user ID
    private static Long testUserId;

    @BeforeAll
    @Step("Global setup: Initializing services and creating a test user")
    static void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        AuthenticationStrategy authStrategy = new BearerTokenAuthentication(client);
        userService = new UserService(authStrategy);

        // Reset zip codes to a known state
        Allure.step("Resetting zip codes to default values");
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));

        // Create a user to update
        Allure.step("Creating a test user: TestUser");
        User user = User.builder()
                .name("TestUser")
                .email("testuser@example.com")
                .sex("Male")
                .age(25)
                .zipCode("10001")
                .build();

        ApiResponse response = userService.createUser(user);
        attachResponseDetails(response);
        assertEquals(201, response.getStatusCode(), "User creation failed");

        // Parse response body to get user ID
        JSONObject jsonResponse = new JSONObject(response.getResponseBody());
        testUserId = jsonResponse.getLong("id");
    }

    @Test
    @Order(1)
    @Story("Successful User Update")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that a user can be updated successfully with valid data.")
    public void testUpdateUser_Success() throws IOException {
        logger.info("Running Scenario #1: Update user successfully");

        User user = User.builder()
                .name("UpdatedName")
                .email("updated@example.com")
                .sex("Female")
                .age(28)
                .zipCode("10001")
                .build();

        // Start Allure step
        Allure.step("Sending PUT request to update user: " + testUserId);

        ApiResponse response = userService.updateUser(testUserId, user);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(200, response.getStatusCode(), "Expected status code 200 for successful update");
    }

    @Test
    @Order(2)
    @Story("Update User with Invalid Zip Code")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to update a user with an invalid zip code to ensure proper error handling.")
    public void testUpdateUser_InvalidZipCode() throws IOException {
        logger.info("Running Scenario #2: Update user with invalid zip code");

        User user = User.builder()
                .name("UpdatedName")
                .email("updated@example.com")
                .sex("Female")
                .age(28)
                .zipCode("99999") // Invalid zip code
                .build();

        // Start Allure step
        Allure.step("Sending PUT request to update user: " + testUserId + " with invalid zip code 99999");

        ApiResponse response = userService.updateUser(testUserId, user);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(424, response.getStatusCode(), "Expected status code 424 for invalid zip code");
    }

    @Test
    @Order(3)
    @Story("Update User with Missing Required Fields")
    @Severity(SeverityLevel.NORMAL)
    @Description("Attempt to update a user without providing all required fields to ensure proper validation.")
    public void testUpdateUser_MissingRequiredFields() throws IOException {
        logger.info("Running Scenario #3: Update user with missing required fields");

        User user = User.builder()
                .email("updated@example.com")
                .age(28)
                .build(); // Missing 'name' and 'sex'

        // Start Allure step
        Allure.step("Sending PUT request to update user: " + testUserId + " with missing required fields");

        ApiResponse response = userService.updateUser(testUserId, user);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(400, response.getStatusCode(), "Expected status code 400 for missing required fields");
    }

    /**
     * Attaches response status code to the Allure report.
     *
     * @param response The ApiResponse object containing status code.
     * @return The status code as a string.
     */
    @Attachment(value = "Response Status Code", type = "text/plain")
    public static String attachStatusCode(ApiResponse response) {
        return String.valueOf(response.getStatusCode());
    }

    /**
     * Attaches response body to the Allure report.
     *
     * @param response The ApiResponse object containing response body.
     * @return The response body as a string.
     */
    @Attachment(value = "Response Body", type = "text/plain")
    public static String attachResponseBody(ApiResponse response) {
        return response.getResponseBody();
    }

    /**
     * Helper method to attach both status code and response body.
     *
     * @param response The ApiResponse object containing status code and response body.
     */
    private static void attachResponseDetails(ApiResponse response) {
        attachStatusCode(response);
        attachResponseBody(response);
    }
}
