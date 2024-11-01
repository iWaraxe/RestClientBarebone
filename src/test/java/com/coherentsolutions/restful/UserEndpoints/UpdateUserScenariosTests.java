package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import com.coherentsolutions.restful.auth.AuthenticationStrategy;
import com.coherentsolutions.restful.auth.BearerTokenAuthentication;
import com.coherentsolutions.restful.auth.OAuth2Client;
import com.coherentsolutions.restful.user.UpdateUserDto;
import com.coherentsolutions.restful.user.User;
import com.coherentsolutions.restful.user.UserDto;
import com.coherentsolutions.restful.user.UserService;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Update User Scenarios")
public class UpdateUserScenariosTests {

    private static final Logger logger = LoggerFactory.getLogger(UpdateUserScenariosTests.class);
    private UserService userService;
    private OAuth2Client client;
    private UserDto userToChange;

    @BeforeEach
    @Step("Setup: Initializing services, resetting zip codes, and creating a test user")
    void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        AuthenticationStrategy authStrategy = new BearerTokenAuthentication(client);
        userService = new UserService(authStrategy);

        // Clean up existing users
        Allure.step("Cleaning up existing users");
        userService.deleteAllUsers();

        // Reset zip codes
        Allure.step("Resetting zip codes to default values");
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));

        // Create a user to update
        Allure.step("Creating a test user: OriginalUser");
        User user = User.builder()
                .name("OriginalUser")
                .email("original@example.com")
                .sex("Male")
                .age(25)
                .zipCode("10001")
                .build();

        ApiResponse response = userService.createUser(user);
        attachResponseDetails(response);
        assertEquals(201, response.getStatusCode(), "User creation failed");

        // Set up userToChange
        userToChange = UserDto.builder()
                .name("OriginalUser")
                .sex("Male")
                .build();
    }

    @Test
    @Order(1)
    @Story("Successful User Update")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that a user can be updated successfully with valid data.")
    public void testUpdateUser_Success() throws IOException {
        logger.info("Running Scenario #1: Successful user update");

        UserDto userNewValues = UserDto.builder()
                .name("UpdatedUser")
                .email("updated@example.com")
                .sex("Female")
                .age(28)
                .zipCode("10001")
                .build();

        UpdateUserDto updateUserDto = new UpdateUserDto(userNewValues, userToChange);

        // Start Allure step
        Allure.step("Sending PUT request to update user with valid data");

        ApiResponse response = userService.updateUser(updateUserDto);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(200, response.getStatusCode(), "Expected status code 200");
    }

    @Test
    @Order(2)
    @Story("Update User with Invalid Zip Code")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to update a user with an invalid zip code to ensure proper error handling.")
    public void testUpdateUser_InvalidZipCode() throws IOException {
        logger.info("Running Scenario #2: Update user with invalid zip code");

        UserDto userNewValues = UserDto.builder()
                .email("updated@example.com")
                .age(28)
                .zipCode("99999") // Invalid zip code
                .build();

        UpdateUserDto updateUserDto = new UpdateUserDto(userNewValues, userToChange);

        // Start Allure step
        Allure.step("Sending PUT request to update user with invalid zip code 99999");

        ApiResponse response = userService.updateUser(updateUserDto);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(424, response.getStatusCode(), "Expected status code 424");
    }

    @Test
    @Order(3)
    @Story("Update User with Missing Required Fields")
    @Severity(SeverityLevel.NORMAL)
    @Description("Attempt to update a user without providing all required fields to ensure proper validation.")
    public void testUpdateUser_MissingRequiredFields() throws IOException {
        logger.info("Running Scenario #3: Update user with missing required fields");

        UserDto userNewValues = UserDto.builder()
                .email("updated@example.com")
                .age(28)
                .build(); // Missing 'name' and 'sex'

        UpdateUserDto updateUserDto = new UpdateUserDto(userNewValues, userToChange);

        // Start Allure step
        Allure.step("Sending PUT request to update user with missing required fields");

        ApiResponse response = userService.updateUser(updateUserDto);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(400, response.getStatusCode(), "Expected status code 400");
    }

    // Additional test methods can be added here following the same pattern

    /**
     * Attaches response status code to the Allure report.
     *
     * @param response The ApiResponse object containing status code.
     * @return The status code as a string.
     */
    @Attachment(value = "Response Status Code", type = "text/plain")
    public String attachStatusCode(ApiResponse response) {
        return String.valueOf(response.getStatusCode());
    }

    /**
     * Attaches response body to the Allure report.
     *
     * @param response The ApiResponse object containing response body.
     * @return The response body as a string.
     */
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
