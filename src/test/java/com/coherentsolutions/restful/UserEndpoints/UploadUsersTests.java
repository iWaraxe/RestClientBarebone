package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UploadUsersTests {

    private static final Logger logger = LoggerFactory.getLogger(UploadUsersTests.class);
    private UserService userService;
    private OAuth2Client client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        userService = new UserService(client);
        objectMapper = new ObjectMapper();

        // Reset zip codes to a known state before each test
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));

        // Clean up existing users
        userService.deleteAllUsers();
    }

    @Test
    void testUploadUsers_Successful() throws IOException {
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
        logger.debug("Uploading JSON file: {}", new String(Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8));
        ApiResponse response = userService.uploadUsers(tempFile);

        // Log response details for debugging
        logger.debug("Response Status Code: {}", response.getStatusCode());
        logger.debug("Response Body: {}", response.getResponseBody());

        assertEquals(201, response.getStatusCode(), "Expected status code 201");
        JSONObject responseBody = new JSONObject(response.getResponseBody());
        assertEquals(2, responseBody.getInt("uploadedUsers"), "Expected 2 uploaded users");

        // Verify users are replaced
        ApiResponse getUsersResponse = userService.getUsers(null);
        JSONArray usersArray = new JSONArray(getUsersResponse.getResponseBody());
        assertEquals(2, usersArray.length(), "Expected 2 users in the system");

        // Clean up temporary file
        Files.deleteIfExists(tempFile.toPath());
    }


    @Test
    void testUploadUsers_UnavailableZipCode() throws IOException {
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
        logger.debug("Uploading JSON file: {}", new String(Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8));
        ApiResponse response = userService.uploadUsers(tempFile);

        // Log response details for debugging
        logger.debug("Response Status Code: {}", response.getStatusCode());
        logger.debug("Response Body: {}", response.getResponseBody());

        assertEquals(424, response.getStatusCode(), "Expected status code 424");

        // Verify users are not replaced
        ApiResponse getUsersResponse = userService.getUsers(null);
        JSONArray usersArray = new JSONArray(getUsersResponse.getResponseBody());
        assertEquals(0, usersArray.length(), "Expected no users in the system");

        // Clean up temporary file
        Files.deleteIfExists(tempFile.toPath());
    }


    @Test
    void testUploadUsers_MissingRequiredField() throws IOException {
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
        logger.debug("Uploading JSON file: {}", new String(Files.readAllBytes(tempFile.toPath()), StandardCharsets.UTF_8));
        ApiResponse response = userService.uploadUsers(tempFile);

        // Log response details for debugging
        logger.debug("Response Status Code: {}", response.getStatusCode());
        logger.debug("Response Body: {}", response.getResponseBody());

        assertEquals(409, response.getStatusCode(), "Expected status code 409");

        // Verify users are not replaced
        ApiResponse getUsersResponse = userService.getUsers(null);
        JSONArray usersArray = new JSONArray(getUsersResponse.getResponseBody());
        assertEquals(0, usersArray.length(), "Expected no users in the system");

        // Clean up temporary file
        Files.deleteIfExists(tempFile.toPath());
    }


    // Helper method for creating a temporary JSON file
    private File createTempJsonFile(List<UploadUserDto> users) throws IOException {
        File tempFile = File.createTempFile("users", ".json");
        objectMapper.writeValue(tempFile, users);
        return tempFile;
    }
}
