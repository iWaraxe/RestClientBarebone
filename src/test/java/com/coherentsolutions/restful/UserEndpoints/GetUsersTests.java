package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.ApiResponse;
import com.coherentsolutions.restful.OAuth2Client;
import com.coherentsolutions.restful.User;
import com.coherentsolutions.restful.UserService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetUsersTests {

    private static final Logger logger = LoggerFactory.getLogger(GetUsersTests.class);
    private static UserService userService;
    private static OAuth2Client client;

    @BeforeAll
    static void globalSetUp() throws IOException {
        // This runs only once before all the tests
        client = OAuth2Client.getInstance(); // Create client only once
        userService = new UserService(client);

        client.resetZipCodes(Arrays.asList("10001", "20002", "30003")); // Only once

        // Clean up users before tests
        userService.deleteAllUsers();

        // Create test users once for GET request (we do not modify the server state)
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
                .age(35)
                .zipCode("20002")
                .build());

        userService.createUser(User.builder()
                .name("Charlie")
                .email("charlie@example.com")
                .sex("Male")
                .age(30)
                .zipCode("30003")
                .build());
    }

    @Test
    @Order(1)
    public void testGetAllUsers() throws IOException {
        logger.info("Running Scenario #1: Get all users");

        ApiResponse response = userService.getUsers(null);
        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);
        assertEquals(3, jsonArray.length(), "Expected 3 users in response");
    }

    @Test
    @Order(2)
    public void testGetUsersOlderThan() throws IOException {
        logger.info("Running Scenario #2: Get users older than a specific age");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("olderThan", "30");

        ApiResponse response = userService.getUsers(queryParams);
        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);

        assertEquals(1, jsonArray.length(), "Expected 1 user in response");
        JSONObject user = jsonArray.getJSONObject(0);
        assertEquals("Bob", user.getString("name"), "Expected user Bob");
        assertTrue(user.getInt("age") > 30, "User's age should be greater than 30");
    }

    @Test
    @Order(3)
    public void testGetUsersYoungerThan() throws IOException {
        logger.info("Running Scenario #3: Get users younger than a specific age");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("youngerThan", "30");

        ApiResponse response = userService.getUsers(queryParams);
        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);

        assertEquals(1, jsonArray.length(), "Expected 1 user in response");
        JSONObject user = jsonArray.getJSONObject(0);
        assertEquals("Alice", user.getString("name"), "Expected user Alice");
        assertTrue(user.getInt("age") < 30, "User's age should be less than 30");
    }

    // Parameterized test for sex-based queries
    @ParameterizedTest
    @Order(3)
    @CsvSource({
            "Male, 2",   // Expect 2 male users
            "Female, 1"  // Expect 1 female user
    })
    public void testGetUsersBySex(String sex, int expectedUserCount) throws IOException {
        logger.info("Running Parameterized Test for sex: " + sex);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("sex", sex);

        ApiResponse response = userService.getUsers(queryParams);
        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);

        assertEquals(expectedUserCount, jsonArray.length(), "Expected " + expectedUserCount + " users in response");
    }

    // Additional Test Ideas

    @Test
    @Order(5)
    public void testGetUsersWithInvalidQueryParameter() throws IOException {
        logger.info("Testing with an invalid query parameter");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("invalidParam", "test");

        ApiResponse response = userService.getUsers(queryParams);

        // Assuming API ignores unknown parameters
        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);
        assertEquals(3, jsonArray.length(), "Expected 3 users in response");
    }

    @Test
    @Order(6)
    public void testGetUsersWithMultipleParameters() throws IOException {
        logger.info("Testing with multiple query parameters: olderThan and sex");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("olderThan", "25");
        queryParams.put("sex", "Male");

        ApiResponse response = userService.getUsers(queryParams);
        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);

        assertEquals(2, jsonArray.length(), "Expected 2 users in response");

        Set<String> expectedNames = new HashSet<>(Arrays.asList("Bob", "Charlie"));
        Set<String> actualNames = new HashSet<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject user = jsonArray.getJSONObject(i);
            actualNames.add(user.getString("name"));
            assertTrue(user.getInt("age") > 25, "User's age should be greater than 25");
            assertEquals("Male", user.getString("sex"), "Expected sex to be Male");
        }
        assertEquals(expectedNames, actualNames, "Expected users Bob and Charlie");
    }

    @Test
    @Order(7)
    public void testGetUsersWithNoMatchingCriteria() throws IOException {
        logger.info("Testing with criteria that match no users");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("olderThan", "100");

        ApiResponse response = userService.getUsers(queryParams);
        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);
        assertEquals(0, jsonArray.length(), "Expected no users in response");
    }

    @Test
    @Order(8)
    public void testGetUsersUnauthorized() throws IOException {
        logger.info("Testing unauthorized access to /users endpoint");

        // Temporarily invalidate the token
        client.invalidateTokens();

        ApiResponse response = userService.getUsers(null);
        assertEquals(401, response.getStatusCode(), "Expected status code 401 for unauthorized access");
        // Validate the tokens
        client.validateTokens();
    }

    @Test
    @Order(9)
    public void testGetUsersWithInvalidToken() throws IOException {
        logger.info("Testing access with invalid token");

        // Set an invalid token
        client.setInvalidToken();

        ApiResponse response = userService.getUsers(null);
        assertEquals(401, response.getStatusCode(), "Expected status code 401 for invalid token");
        // Validate the tokens
        client.validateTokens();
    }


    @Test
    @Order(10)
    public void testGetUsersWithSpecialCharactersInParameters() throws IOException {
        logger.info("Testing with special characters in query parameters");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("sex", "Male&Female");

        ApiResponse response = userService.getUsers(queryParams);

        // Depending on API handling, it may return 400 Bad Request or handle it appropriately
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 400,
                "Expected status code 200 or 400 for special characters in parameters");
    }

    @Test
    @Order(11)
    public void testGetUsersWithInvalidAgeParameter() throws IOException {
        logger.info("Testing with invalid age parameter (non-integer value)");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("olderThan", "abc");

        ApiResponse response = userService.getUsers(queryParams);

        // Expected: 400 Bad Request due to invalid parameter
        assertEquals(400, response.getStatusCode(), "Expected status code 400 for invalid age parameter");

        // If the API does not return 400, report a bug
        if (response.getStatusCode() != 400) {
            // Create a bug report
            String bugReport = generateBugReport("Send GET request to /users with olderThan=abc",
                    "API returned status code " + response.getStatusCode(),
                    "API should return 400 Bad Request for invalid 'olderThan' parameter",
                    "Response body: " + response.getResponseBody());
            logger.error("Bug Found:\n{}", bugReport);
            fail("Bug found: API does not handle invalid 'olderThan' parameter correctly");
        }
    }

    // Helper method to generate bug reports
    private String generateBugReport(String steps, String actualResult, String expectedResult, String logs) {
        return String.format("**Bug Report**\n\n**Steps to Reproduce:**\n%s\n\n**Actual Result:**\n%s\n\n**Expected Result:**\n%s\n\n**Logs:**\n%s",
                steps, actualResult, expectedResult, logs);
    }
}
