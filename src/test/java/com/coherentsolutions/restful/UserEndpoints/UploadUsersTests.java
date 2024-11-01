package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Upload Users")
public class UploadUsersTests {

    private static final Logger logger = LoggerFactory.getLogger(UploadUsersTests.class);
    private UserService userService;
    private OAuth2Client client;
    private ObjectMapper objectMapper;

    @BeforeEach
    @Step("Setup: Initializing services, resetting zip codes, and cleaning up users")
    void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        AuthenticationStrategy authStrategy = new BearerTokenAuthentication(client);
        userService = new UserService(authStrategy);
        objectMapper = new ObjectMapper();

        // Clean up existing users
        Allure.step("Cleaning up existing users");
        userService.deleteAllUsers();

        // Reset zip codes to a known state before each test
        Allure.step("Resetting zip codes to default values");
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));
    }

    @Test
    @Order(1)
    @Story("Successful Upload")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that users can be uploaded successfully with valid data.")
    public void testUploadUsers_Successful() throws IOException {
        logger.info("Running Scenario #1: Successful Upload");

        List<UploadUserDto> users = Arrays.asList(
                UploadUserDto.builder()
                        .name("Alice")
                        .email("alice@example.com")
                        .sex("Female")
                        .age(25)
                        .zipCode(ZipCodeDto.builder().code("10001").build())
                        .build(),
                UploadUserDto.builder()
                        .name("Bob")
                        .email("bob@example.com")
                        .sex("Male")
                        .age(30)
                        .zipCode(ZipCodeDto.builder().code("20002").build())
                        .build()
        );

        File tempFile = createTempJsonFile(users);
        Allure.step("Uploading JSON file with valid users");
        logger.debug("Uploading JSON file: {}", new String(Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8));
        ApiResponse response = userService.uploadUsers(tempFile);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(201, response.getStatusCode(), "Expected status code 201");
        JSONObject responseBody = new JSONObject(response.getResponseBody());
        assertEquals(2, responseBody.getInt("uploadedUsers"), "Expected 2 uploaded users");

        // Verify users are replaced
        Allure.step("Verifying that users are uploaded correctly");
        ApiResponse getUsersResponse = userService.getUsers(null);
        JSONArray usersArray = new JSONArray(getUsersResponse.getResponseBody());
        assertEquals(2, usersArray.length(), "Expected 2 users in the system");

        // Clean up temporary file
        Files.deleteIfExists(tempFile.toPath());
    }


    @Test
    @Order(2)
    @Story("Upload with Unavailable Zip Code")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to upload a user with an unavailable zip code to ensure proper error handling.")
    public void testUploadUsers_UnavailableZipCode() throws IOException {
        logger.info("Running Scenario #2: Upload with Unavailable Zip Code");

        List<UploadUserDto> users = Arrays.asList(
                UploadUserDto.builder()
                        .name("Charlie")
                        .email("charlie@example.com")
                        .sex("Male")
                        .age(28)
                        .zipCode(ZipCodeDto.builder().code("99999").build()) // Unavailable Zip Code
                        .build()
        );

        File tempFile = createTempJsonFile(users);
        Allure.step("Uploading JSON file with unavailable zip code");
        logger.debug("Uploading JSON file: {}", new String(Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8));
        ApiResponse response = userService.uploadUsers(tempFile);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(424, response.getStatusCode(), "Expected status code 424");

        // Verify users are not replaced
        Allure.step("Verifying that users are not uploaded due to invalid zip code");
        ApiResponse getUsersResponse = userService.getUsers(null);
        JSONArray usersArray = new JSONArray(getUsersResponse.getResponseBody());
        assertEquals(0, usersArray.length(), "Expected no users in the system");

        // Clean up temporary file
        Files.deleteIfExists(tempFile.toPath());
    }


    @Test
    @Order(3)
    @Story("Upload with Missing Required Field")
    @Severity(SeverityLevel.NORMAL)
    @Description("Attempt to upload a user without required fields to ensure proper validation.")
    public void testUploadUsers_MissingRequiredField() throws IOException {
        logger.info("Running Scenario #3: Upload with Missing Required Field");

        List<UploadUserDto> users = Arrays.asList(
                UploadUserDto.builder()
                        .email("dana@example.com")
                        .sex("Female")
                        .age(26)
                        .zipCode(ZipCodeDto.builder().code("10001").build()) // Missing name
                        .build()
        );

        File tempFile = createTempJsonFile(users);
        Allure.step("Uploading JSON file with missing required fields");
        logger.debug("Uploading JSON file: {}", new String(Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8));
        ApiResponse response = userService.uploadUsers(tempFile);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(409, response.getStatusCode(), "Expected status code 409");

        // Verify users are not replaced
        Allure.step("Verifying that users are not uploaded due to missing required fields");
        ApiResponse getUsersResponse = userService.getUsers(null);
        JSONArray usersArray = new JSONArray(getUsersResponse.getResponseBody());
        assertEquals(0, usersArray.length(), "Expected no users in the system");

        // Clean up temporary file
        Files.deleteIfExists(tempFile.toPath());
    }


    // Helper method for creating a temporary JSON file
    @Step("Creating temporary JSON file with users data")
    private File createTempJsonFile(List<UploadUserDto> users) throws IOException {
        File tempFile = File.createTempFile("users", ".json");
        objectMapper.writeValue(tempFile, users);
        return tempFile;
    }

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
    @Step("Attaching response details")
    private void attachResponseDetails(ApiResponse response) {
        attachStatusCode(response);
        attachResponseBody(response);
    }
}
