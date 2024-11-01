package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostUsersTests {

    private static final Logger logger = LoggerFactory.getLogger(PostUsersTests.class);
    private UserService userService;
    private OAuth2Client client;

    @BeforeEach
    void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        AuthenticationStrategy authStrategy = new BearerTokenAuthentication(client);
        userService = new UserService(authStrategy);

        // Reset zip codes to known state before each test
        client.resetZipCodes(Arrays.asList("10001", "20002", "30003"));
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up users after each test
        userService.deleteAllUsers();
    }

    @Test
    @Order(1)
    public void testCreateUser_AllFieldsFilled_ValidZipCode() throws IOException {
        logger.info("Running Scenario #1: All fields filled, valid zip code");

        User user = User.builder()
                .name("Alice")
                .email("alice@example.com")
                .sex("Female")
                .zipCode("10001")
                .age(25) // Ensure age is included if required
                .build();

        ApiResponse response = userService.createUser(user);
        int statusCode = response.getStatusCode();
        String responseBody = response.getResponseBody();

        assertEquals(201, statusCode, "Expected status code 201");
        assertNotNull(responseBody, "Response body should not be null");
        assertTrue(responseBody.contains("Alice"), "Response should contain user name");

        // Verify that the zip code is now unavailable
        assertFalse(client.getAvailableZipCodes().contains("10001"), "Zip code 10001 should be unavailable after user creation");
    }

    @Test
    @Order(2)
    public void testCreateUser_RequiredFieldsOnly() throws IOException {
        logger.info("Running Scenario #2: Required fields only");

        User user = User.builder()
                .name("Bob")
                .sex("Male")
                .build();

        ApiResponse response = userService.createUser(user);
        int statusCode = response.getStatusCode();
        String responseBody = response.getResponseBody();

        assertEquals(201, statusCode, "Expected status code 201");
        assertNotNull(responseBody, "Response body should not be null");
        assertTrue(responseBody.contains("Bob"), "Response should contain user name");
    }

    @Test
    @Order(3)
    public void testCreateUser_InvalidZipCode() throws IOException {
        logger.info("Running Scenario #3: Invalid (unavailable) zip code");

        User user = User.builder()
                .name("Charlie")
                .email("charlie@example.com")
                .sex("Male")
                .zipCode("99999") // Invalid zip code
                .build();

        ApiResponse response = userService.createUser(user);
        int statusCode = response.getStatusCode();

        assertEquals(424, statusCode, "Expected status code 424");
    }

    @Test
    @Order(4)
    public void testCreateUser_DuplicateNameAndSex() throws IOException {
        logger.info("Running Scenario #4: Duplicate name and sex");

        // First, create the initial user
        User user1 = User.builder()
                .name("Dana")
                .sex("Female")
                .build();

        ApiResponse response1 = userService.createUser(user1);
        assertEquals(201, response1.getStatusCode(), "Initial user creation should succeed");

        // Attempt to create a duplicate user
        User user2 = User.builder()
                .name("Dana")
                .sex("Female")
                .build();

        ApiResponse response2 = userService.createUser(user2);
        int statusCode = response2.getStatusCode();

        assertEquals(409, statusCode, "Expected status code 409 for duplicate user");
    }

    @Test
    @Order(5)
    public void testCreateUser_EmptyRequestBody() throws IOException {
        logger.info("Running Scenario: Empty request body");

        ApiResponse response = userService.createUser(new User());
        assertEquals(400, response.getStatusCode(), "Expected status code 400 for empty request body");
    }

    @Test
    @Order(6)
    public void testCreateUser_InvalidEmailFormat() throws IOException {
        logger.info("Running Scenario: Invalid email format");

        User user = User.builder()
                .name("Eve")
                .email("not-an-email")
                .sex("Female")
                .build();

        ApiResponse response = userService.createUser(user);
        assertEquals(400, response.getStatusCode(), "Expected status code 400 for invalid email format");
    }

    @Test
    @Order(7)
    public void testCreateUser_MissingNameField() throws IOException {
        logger.info("Running Scenario: Missing name field");

        User user = User.builder()
                .sex("Male")
                .build(); // Name is missing

        ApiResponse response = userService.createUser(user);
        assertEquals(400, response.getStatusCode(), "Expected status code 400 for missing name field");
    }

    @Test
    @Order(8)
    public void testCreateUser_LargePayload() throws IOException {
        logger.info("Running Scenario: Large payload");

        String largeName = new String(new char[10000]).replace("\0", "A"); // Large name
        User user = User.builder()
                .name(largeName)
                .email("large@example.com")
                .sex("Male")
                .build();

        ApiResponse response = userService.createUser(user);
        assertTrue(response.getStatusCode() == 201 || response.getStatusCode() == 400,
                "Expected either 201 or 400 for large payload");
    }

    @Test
    @Order(9)
    public void testCreateUser_InvalidHttpMethod() throws IOException {
        logger.info("Running Scenario: Invalid HTTP method");

        // Implement a GET/TRACE/PATCH request or other invalid method to `/users` endpoint
        ApiResponse response = userService.sendInvalidMethodRequest();
        assertEquals(405, response.getStatusCode(), "Expected status code 405 for invalid HTTP method");
    }

    @Test
    @Order(10)
    public void testCreateUser_SpecialCharactersInName() throws IOException {
        logger.info("Running Scenario: Special characters in name");

        User user = User.builder()
                .name("<script>alert('XSS')</script>")
                .email("special@example.com")
                .sex("Male")
                .build();

        ApiResponse response = userService.createUser(user);
        assertTrue(response.getStatusCode() == 201 || response.getStatusCode() == 400,
                "Expected either 201 or 400 for special characters in name");
    }

}
