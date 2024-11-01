package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import com.coherentsolutions.restful.auth.AuthenticationStrategy;
import com.coherentsolutions.restful.auth.BearerTokenAuthentication;
import com.coherentsolutions.restful.auth.OAuth2Client;
import com.coherentsolutions.restful.user.User;
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
@Feature("Create User")
public class PostUsersTests {

    private static final Logger logger = LoggerFactory.getLogger(PostUsersTests.class);
    private UserService userService;
    private OAuth2Client client;

    @BeforeEach
    @Step("Setting up the test environment")
    void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        AuthenticationStrategy authStrategy = new BearerTokenAuthentication(client);
        userService = new UserService(authStrategy);

        // Reset zip codes to known state before each test
        Allure.step("Resetting zip codes to default");
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));
    }

    @AfterEach
    @Step("Tearing down the test environment")
    void tearDown() throws IOException {
        // Clean up users after each test
        Allure.step("Cleaning up users after test execution");
        userService.deleteAllUsers();
    }

    @Test
    @Order(1)
    @Story("Create User with All Fields")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that a user can be created successfully when all required fields are provided and the zip code is valid.")
    public void testCreateUser_AllFieldsFilled_ValidZipCode() throws IOException {
        logger.info("Running Scenario #1: All fields filled, valid zip code");

        User user = User.builder()
                .name("Alice")
                .email("alice@example.com")
                .sex("Female")
                .zipCode("10001")
                .age(25) // Ensure age is included if required
                .build();

        // Start Allure step
        Allure.step("Sending POST request to create user: Alice with all fields filled");

        ApiResponse response = userService.createUser(user);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(201, response.getStatusCode(), "Expected status code 201");
        assertNotNull(response.getResponseBody(), "Response body should not be null");
        assertTrue(response.getResponseBody().contains("Alice"), "Response should contain user name");

        // Verify that the zip code is now unavailable
        Allure.step("Verifying that zip code 10001 is unavailable after user creation");
        assertFalse(client.getAvailableZipCodes().contains("10001"), "Zip code 10001 should be unavailable after user creation");
    }

    @Test
    @Order(2)
    @Story("Create User with Required Fields Only")
    @Severity(SeverityLevel.NORMAL)
    @Description("Ensure that a user can be created successfully when only required fields are provided.")
    public void testCreateUser_RequiredFieldsOnly() throws IOException {
        logger.info("Running Scenario #2: Required fields only");

        User user = User.builder()
                .name("Bob")
                .sex("Male")
                .build();

        // Start Allure step
        Allure.step("Sending POST request to create user: Bob with required fields only");

        ApiResponse response = userService.createUser(user);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(201, response.getStatusCode(), "Expected status code 201");
        assertNotNull(response.getResponseBody(), "Response body should not be null");
        assertTrue(response.getResponseBody().contains("Bob"), "Response should contain user name");
    }

    @Test
    @Order(3)
    @Story("Create User with Invalid Zip Code")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to create a user with an invalid zip code to ensure proper error handling.")
    public void testCreateUser_InvalidZipCode() throws IOException {
        logger.info("Running Scenario #3: Invalid (unavailable) zip code");

        User user = User.builder()
                .name("Charlie")
                .email("charlie@example.com")
                .sex("Male")
                .zipCode("99999") // Invalid zip code
                .build();

        // Start Allure step
        Allure.step("Sending POST request to create user: Charlie with invalid zip code 99999");

        ApiResponse response = userService.createUser(user);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(424, response.getStatusCode(), "Expected status code 424");
    }

    @Test
    @Order(4)
    @Story("Create Duplicate User")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Attempt to create a duplicate user with the same name and sex to ensure proper error handling.")
    public void testCreateUser_DuplicateNameAndSex() throws IOException {
        logger.info("Running Scenario #4: Duplicate name and sex");

        // First, create the initial user
        User user1 = User.builder()
                .name("Dana")
                .sex("Female")
                .build();

        // Start Allure step
        Allure.step("Creating initial user: Dana");

        ApiResponse response1 = userService.createUser(user1);
        attachResponseDetails(response1);
        assertEquals(201, response1.getStatusCode(), "Initial user creation should succeed");

        // Attempt to create a duplicate user
        User user2 = User.builder()
                .name("Dana")
                .sex("Female")
                .build();

        // Start Allure step
        Allure.step("Attempting to create duplicate user: Dana");

        ApiResponse response2 = userService.createUser(user2);
        attachResponseDetails(response2);

        assertEquals(409, response2.getStatusCode(), "Expected status code 409 for duplicate user");
    }

    @Test
    @Order(5)
    @Story("Create User with Empty Request Body")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to create a user with an empty request body to ensure proper error handling.")
    public void testCreateUser_EmptyRequestBody() throws IOException {
        logger.info("Running Scenario #5: Empty request body");

        // Start Allure step
        Allure.step("Sending POST request with empty request body");

        ApiResponse response = userService.createUser(new User());

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(400, response.getStatusCode(), "Expected status code 400 for empty request body");
    }

    @Test
    @Order(6)
    @Story("Create User with Invalid Email Format")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to create a user with an invalid email format to ensure proper validation.")
    public void testCreateUser_InvalidEmailFormat() throws IOException {
        logger.info("Running Scenario #6: Invalid email format");

        User user = User.builder()
                .name("Eve")
                .email("not-an-email")
                .sex("Female")
                .build();

        // Start Allure step
        Allure.step("Sending POST request to create user: Eve with invalid email format");

        ApiResponse response = userService.createUser(user);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(400, response.getStatusCode(), "Expected status code 400 for invalid email format");
    }

    @Test
    @Order(7)
    @Story("Create User with Missing Name Field")
    @Severity(SeverityLevel.NORMAL)
    @Description("Attempt to create a user without the name field to ensure proper validation.")
    public void testCreateUser_MissingNameField() throws IOException {
        logger.info("Running Scenario #7: Missing name field");

        User user = User.builder()
                .sex("Male")
                .build(); // Name is missing

        // Start Allure step
        Allure.step("Sending POST request to create user with missing name field");

        ApiResponse response = userService.createUser(user);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(400, response.getStatusCode(), "Expected status code 400 for missing name field");
    }

    @Test
    @Order(8)
    @Story("Create User with Large Payload")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to create a user with a large payload to test API's handling of oversized requests.")
    public void testCreateUser_LargePayload() throws IOException {
        logger.info("Running Scenario #8: Large payload");

        String largeName = new String(new char[10000]).replace("\0", "A"); // Large name
        User user = User.builder()
                .name(largeName)
                .email("large@example.com")
                .sex("Male")
                .build();

        // Start Allure step
        Allure.step("Sending POST request to create user: " + largeName + " with large payload");

        ApiResponse response = userService.createUser(user);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertTrue(response.getStatusCode() == 201 || response.getStatusCode() == 400,
                "Expected either 201 or 400 for large payload");
    }

    @Test
    @Order(9)
    @Story("Create User with Invalid HTTP Method")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to use an unsupported HTTP method to create a user and ensure proper error handling.")
    public void testCreateUser_InvalidHttpMethod() throws IOException {
        logger.info("Running Scenario #9: Invalid HTTP method");

        // Start Allure step
        Allure.step("Sending POST request using invalid HTTP method");

        // Implement a GET/TRACE/PATCH request or other invalid method to `/users` endpoint
        ApiResponse response = userService.sendInvalidMethodRequest();

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertEquals(405, response.getStatusCode(), "Expected status code 405 for invalid HTTP method");
    }

    @Test
    @Order(10)
    @Story("Create User with Special Characters in Name")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to create a user with special characters in the name to test input sanitization.")
    public void testCreateUser_SpecialCharactersInName() throws IOException {
        logger.info("Running Scenario #10: Special characters in name");

        User user = User.builder()
                .name("<script>alert('XSS')</script>")
                .email("special@example.com")
                .sex("Male")
                .build();

        // Start Allure step
        Allure.step("Sending POST request to create user with special characters in name");

        ApiResponse response = userService.createUser(user);

        // Attach response details to Allure report
        attachResponseDetails(response);

        assertTrue(response.getStatusCode() == 201 || response.getStatusCode() == 400,
                "Expected either 201 or 400 for special characters in name");
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
