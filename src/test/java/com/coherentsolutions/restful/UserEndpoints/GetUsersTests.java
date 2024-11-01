package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import com.coherentsolutions.restful.auth.AuthenticationStrategy;
import com.coherentsolutions.restful.auth.BearerTokenAuthentication;
import com.coherentsolutions.restful.auth.OAuth2Client;
import com.coherentsolutions.restful.user.User;
import com.coherentsolutions.restful.user.UserService;
import io.qameta.allure.*;
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
@Epic("User Management")
@Feature("Get Users")
public class GetUsersTests {

    private static final Logger logger = LoggerFactory.getLogger(GetUsersTests.class);
    private static UserService userService;
    private static OAuth2Client client;

    @BeforeAll
    @Step("Global setup before all tests")
    static void globalSetUp() throws IOException {
        // This runs only once before all the tests
        client = OAuth2Client.getInstance(); // Create client only once
        AuthenticationStrategy authStrategy = new BearerTokenAuthentication(client);
        userService = new UserService(authStrategy);

        // Reset zip codes
        Allure.step("Resetting zip codes to default");
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003")); // Only once

        // Clean up users before tests
        Allure.step("Cleaning up existing users");
        userService.deleteAllUsers();

        // Create test users once for GET request (we do not modify the server state)
        Allure.step("Creating test users: Alice, Bob, and Charlie");
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
    @Story("Get All Users")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Retrieve all users and verify the count and details.")
    public void testGetAllUsers() throws IOException {
        logger.info("Running Scenario #1: Get all users");

        // Start Allure step
        Allure.step("Sending GET request to retrieve all users");

        ApiResponse response = userService.getUsers(null);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);
        assertEquals(3, jsonArray.length(), "Expected 3 users in response");

        // Additional verification can be added here if needed
    }

    @Test
    @Order(2)
    @Story("Get Users Older Than a Specific Age")
    @Severity(SeverityLevel.NORMAL)
    @Description("Retrieve users older than a specified age and verify the results.")
    public void testGetUsersOlderThan() throws IOException {
        logger.info("Running Scenario #2: Get users older than a specific age");

        // Define query parameters
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("olderThan", "30");

        // Start Allure step
        Allure.step("Sending GET request with query parameter olderThan=30");

        ApiResponse response = userService.getUsers(queryParams);

        // Attach response details to Allure report
        attachResponseDetails(response);

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
    @Story("Get Users Younger Than a Specific Age")
    @Severity(SeverityLevel.NORMAL)
    @Description("Retrieve users younger than a specified age and verify the results.")
    public void testGetUsersYoungerThan() throws IOException {
        logger.info("Running Scenario #3: Get users younger than a specific age");

        // Define query parameters
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("youngerThan", "30");

        // Start Allure step
        Allure.step("Sending GET request with query parameter youngerThan=30");

        ApiResponse response = userService.getUsers(queryParams);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);

        assertEquals(1, jsonArray.length(), "Expected 1 user in response");
        JSONObject user = jsonArray.getJSONObject(0);
        assertEquals("Alice", user.getString("name"), "Expected user Alice");
        assertTrue(user.getInt("age") < 30, "User's age should be less than 30");
    }

    @ParameterizedTest
    @Order(4)
    @CsvSource({
            "Male, 2",   // Expect 2 male users
            "Female, 1"  // Expect 1 female user
    })
    @Story("Get Users by Sex")
    @Severity(SeverityLevel.NORMAL)
    @Description("Retrieve users based on sex and verify the count of users returned.")
    public void testGetUsersBySex(String sex, int expectedUserCount) throws IOException {
        logger.info("Running Parameterized Test for sex: " + sex);

        // Define query parameters
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("sex", sex);

        // Start Allure step
        Allure.step("Sending GET request with query parameter sex=" + sex);

        ApiResponse response = userService.getUsers(queryParams);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);

        assertEquals(expectedUserCount, jsonArray.length(), "Expected " + expectedUserCount + " users in response");

        // Verify each user's sex
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject user = jsonArray.getJSONObject(i);
            assertEquals(sex, user.getString("sex"), "Expected sex to be " + sex);
        }
    }

    // Additional Test Ideas

    @Test
    @Order(5)
    @Story("Get Users with Invalid Query Parameter")
    @Severity(SeverityLevel.MINOR)
    @Description("Send a GET request with an invalid query parameter and verify API behavior.")
    public void testGetUsersWithInvalidQueryParameter() throws IOException {
        logger.info("Running Scenario #5: Get users with an invalid query parameter");

        // Define invalid query parameters
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("invalidParam", "test");

        // Start Allure step
        Allure.step("Sending GET request with invalid query parameter invalidParam=test");

        ApiResponse response = userService.getUsers(queryParams);

        // Attach response details to Allure report
        attachResponseDetails(response);

        // Assuming API ignores unknown parameters and returns all users
        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);
        assertEquals(3, jsonArray.length(), "Expected 3 users in response");
    }

    @Test
    @Order(6)
    @Story("Get Users with Multiple Query Parameters")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Send a GET request with multiple query parameters and verify the filtered results.")
    public void testGetUsersWithMultipleParameters() throws IOException {
        logger.info("Running Scenario #6: Get users with multiple query parameters: olderThan and sex");

        // Define multiple query parameters
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("olderThan", "25");
        queryParams.put("sex", "Male");

        // Start Allure step
        Allure.step("Sending GET request with query parameters olderThan=25 and sex=Male");

        ApiResponse response = userService.getUsers(queryParams);

        // Attach response details to Allure report
        attachResponseDetails(response);

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
    @Story("Get Users with No Matching Criteria")
    @Severity(SeverityLevel.NORMAL)
    @Description("Send a GET request with criteria that match no users and verify the response.")
    public void testGetUsersWithNoMatchingCriteria() throws IOException {
        logger.info("Running Scenario #7: Get users with criteria that match no users");

        // Define query parameters that match no users
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("olderThan", "100");

        // Start Allure step
        Allure.step("Sending GET request with query parameter olderThan=100");

        ApiResponse response = userService.getUsers(queryParams);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(200, response.getStatusCode(), "Expected status code 200");

        String responseBody = response.getResponseBody();
        JSONArray jsonArray = new JSONArray(responseBody);
        assertEquals(0, jsonArray.length(), "Expected no users in response");
    }

    @Test
    @Order(8)
    @Story("Get Users Unauthorized")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Attempt to retrieve users without proper authorization and verify the response.")
    public void testGetUsersUnauthorized() throws IOException {
        logger.info("Running Scenario #8: Unauthorized access to get users");

        // Invalidate tokens to simulate unauthorized access
        Allure.step("Invalidating tokens to simulate unauthorized access");
        client.invalidateTokens();

        // Start Allure step
        Allure.step("Sending unauthorized GET request to retrieve users");

        ApiResponse response = userService.getUsers(null);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(401, response.getStatusCode(), "Expected status code 401 for unauthorized access");

        // Validate the tokens
        Allure.step("Validating tokens after unauthorized attempt");
        client.validateTokens();
    }

    @Test
    @Order(9)
    @Story("Get Users with Invalid Token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Attempt to retrieve users using an explicitly invalid token and verify the response.")
    public void testGetUsersWithInvalidToken() throws IOException {
        logger.info("Running Scenario #9: Get users with invalid token");

        // Set an invalid token
        Allure.step("Setting an explicitly invalid token");
        client.setInvalidToken();

        // Start Allure step
        Allure.step("Sending GET request with invalid token to retrieve users");

        ApiResponse response = userService.getUsers(null);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(401, response.getStatusCode(), "Expected status code 401 for invalid token");

        // Validate the tokens
        Allure.step("Validating tokens after invalid token usage");
        client.validateTokens();
    }

    @Test
    @Order(10)
    @Story("Get Users with Special Characters in Parameters")
    @Severity(SeverityLevel.MINOR)
    @Description("Send a GET request with special characters in query parameters and verify API handling.")
    public void testGetUsersWithSpecialCharactersInParameters() throws IOException {
        logger.info("Running Scenario #10: Get users with special characters in query parameters");

        // Define query parameters with special characters
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("sex", "Male&Female");

        // Start Allure step
        Allure.step("Sending GET request with special characters in query parameter sex=Male&Female");

        ApiResponse response = userService.getUsers(queryParams);

        // Attach response details to Allure report
        attachResponseDetails(response);

        // Depending on API handling, it may return 400 Bad Request or handle it appropriately
        assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 400,
                "Expected status code 200 or 400 for special characters in parameters");
    }

    @Test
    @Order(11)
    @Story("Get Users with Invalid Age Parameter")
    @Severity(SeverityLevel.MINOR)
    @Description("Send a GET request with a non-integer value for age parameter and verify API response.")
    public void testGetUsersWithInvalidAgeParameter() throws IOException {
        logger.info("Running Scenario #11: Get users with invalid age parameter (non-integer value)");

        // Define query parameters with invalid age value
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("olderThan", "abc");

        // Start Allure step
        Allure.step("Sending GET request with invalid age parameter olderThan=abc");

        ApiResponse response = userService.getUsers(queryParams);

        // Attach response details to Allure report
        attachResponseDetails(response);

        // Expected: 400 Bad Request due to invalid parameter
        assertEquals(400, response.getStatusCode(), "Expected status code 400 for invalid age parameter");
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
