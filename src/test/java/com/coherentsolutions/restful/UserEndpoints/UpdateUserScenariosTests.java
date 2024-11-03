package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.config.RestAssuredConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Update User Scenarios")
public class UpdateUserScenariosTests extends RestAssuredConfig {

    private static final Logger logger = LoggerFactory.getLogger(UpdateUserScenariosTests.class);
    private static String writeToken;
    private static Long testUserId;

    /**
     * Sets up the testing environment before each test.
     * This includes cleaning up existing users, resetting zip codes, and creating a new test user.
     */
    @BeforeEach
    @Step("Setup: Initializing RestAssured, resetting zip codes, and creating a test user")
    void setUp() {
        // Retrieve the write token using the helper method from RestAssuredConfig
        writeToken = getOAuthToken("write");

        // Log the token retrieval
        logger.info("Retrieved write token: {}", writeToken);
        Allure.step("Retrieved write token");

        // Clean up existing users by sending a DELETE request to the appropriate endpoint
        Allure.step("Cleaning up existing users");
        Response deleteResponse = given()
                .auth().oauth2(writeToken)
                .contentType("application/json")
                .when()
                .delete(API_BASE_URL + "/users/all"); // Adjust endpoint as per your API

        attachResponseDetails(deleteResponse);
        deleteResponse.then().statusCode(anyOf(is(200), is(204)));

        // Reset zip codes to a known state
        Allure.step("Resetting zip codes to default values");
        Response resetZipCodesResponse = given()
                .auth().oauth2(writeToken)
                .contentType("application/json")
                .body(Arrays.asList("10001", "20002", "30003"))
                .when()
                .post(API_BASE_URL + "/zip-codes/reset"); // Adjust endpoint as per your API

        attachResponseDetails(resetZipCodesResponse);
        resetZipCodesResponse.then().statusCode(200);

        // Create a test user
        Allure.step("Creating a test user: OriginalUser");
        String userJson = """
            {
                "name": "OriginalUser",
                "email": "original@example.com",
                "sex": "Male",
                "age": 25,
                "zipCode": "10001"
            }""";

        Response createUserResponse = given()
                .auth().oauth2(writeToken)
                .contentType("application/json")
                .body(userJson)
                .log().all() // Log the request for debugging
                .when()
                .post(API_BASE_URL + "/users"); // Adjust endpoint as per your API

        attachResponseDetails(createUserResponse);
        createUserResponse.then().statusCode(201);

        // Parse the response body to extract the user ID
        JSONObject jsonResponse = new JSONObject(createUserResponse.getBody().asString());
        testUserId = jsonResponse.getLong("id");

        // Log the created user ID
        logger.info("Created test user with ID: {}", testUserId);
        Allure.step("Created test user with ID: " + testUserId);
    }

    /**
     * Test Case 1: Successful User Update
     * Ensures that a user can be updated successfully with valid data.
     */
    @Test
    @Order(1)
    @Story("Successful User Update")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that a user can be updated successfully with valid data.")
    public void testUpdateUser_Success() {
        logger.info("Running Scenario #1: Successful user update");

        String updatedUserJson = """
            {
                "name": "UpdatedUser",
                "email": "updated@example.com",
                "sex": "Female",
                "age": 28,
                "zipCode": "10001"
            }""";

        // Start Allure step
        Allure.step("Sending PUT request to update user: " + testUserId);

        // Send PUT request to update the user
        Response updateResponse = given()
                .auth().oauth2(writeToken)
                .contentType("application/json")
                .body(updatedUserJson)
                .log().all() // Log the request for debugging
                .when()
                .put(API_BASE_URL + "/users/" + testUserId); // Adjust endpoint as per your API

        // Attach response details to Allure report
        attachResponseDetails(updateResponse);

        // Assert that the response status code is 200
        updateResponse.then()
                .statusCode(200)
                .log().all(); // Log the response for debugging

        // Optionally, verify the updated user details by retrieving all users and filtering
        Allure.step("Verifying the updated user details");

        Response getUsersResponse = given()
                .auth().oauth2(writeToken)
                .contentType("application/json")
                .when()
                .get(API_BASE_URL + "/users"); // Adjust endpoint as per your API

        attachResponseDetails(getUsersResponse);
        getUsersResponse.then().statusCode(200).log().all();

        // Parse the response to find the updated user
        JSONObject updatedUser = getUserById(getUsersResponse, testUserId);

        // Assert updated fields
        assertNotNull(updatedUser, "Updated user should not be null");
        assertEquals("UpdatedUser", updatedUser.getString("name"), "User name should be updated");
        assertEquals("updated@example.com", updatedUser.getString("email"), "User email should be updated");
        assertEquals("Female", updatedUser.getString("sex"), "User sex should be updated");
        assertEquals(28, updatedUser.getInt("age"), "User age should be updated");
        assertEquals("10001", updatedUser.getJSONObject("zipCode").getString("code"), "User zipCode should remain the same");
    }

    /**
     * Test Case 2: Update User with Invalid Zip Code
     * Attempts to update a user with an invalid zip code to ensure proper error handling.
     */
    @Test
    @Order(2)
    @Story("Update User with Invalid Zip Code")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to update a user with an invalid zip code to ensure proper error handling.")
    public void testUpdateUser_InvalidZipCode() {
        logger.info("Running Scenario #2: Update user with invalid zip code");

        String updatedUserJson = """
            {
                "name": "UpdatedUser",
                "email": "updated@example.com",
                "sex": "Female",
                "age": 28,
                "zipCode": "99999"
            }""";

        // Start Allure step
        Allure.step("Sending PUT request to update user: " + testUserId + " with invalid zip code 99999");

        // Send PUT request with invalid zip code
        Response updateResponse = given()
                .auth().oauth2(writeToken)
                .contentType("application/json")
                .body(updatedUserJson)
                .log().all() // Log the request for debugging
                .when()
                .put(API_BASE_URL + "/users/" + testUserId); // Adjust endpoint as per your API

        // Attach response details to Allure report
        attachResponseDetails(updateResponse);

        // Assert that the response status code is 424 and contains the appropriate error message
        updateResponse.then()
                .statusCode(424)
                .body(containsString("Zip code is unavailable"))
                .log().all(); // Log the response for debugging
    }

    /**
     * Test Case 3: Update User with Missing Required Fields
     * Attempts to update a user without providing all required fields to ensure proper validation.
     */
    @Test
    @Order(3)
    @Story("Update User with Missing Required Fields")
    @Severity(SeverityLevel.NORMAL)
    @Description("Attempt to update a user without providing all required fields to ensure proper validation.")
    public void testUpdateUser_MissingRequiredFields() {
        logger.info("Running Scenario #3: Update user with missing required fields");

        String updatedUserJson = """
            {
                "email": "updated@example.com",
                "age": 28
            }""";

        // Start Allure step
        Allure.step("Sending PUT request to update user: " + testUserId + " with missing required fields");

        // Send PUT request with missing required fields
        Response updateResponse = given()
                .auth().oauth2(writeToken)
                .contentType("application/json")
                .body(updatedUserJson)
                .log().all() // Log the request for debugging
                .when()
                .put(API_BASE_URL + "/users/" + testUserId); // Adjust endpoint as per your API

        // Attach response details to Allure report
        attachResponseDetails(updateResponse);

        // Assert that the response status code is 400 and contains the appropriate error message
        updateResponse.then()
                .statusCode(400)
                .body(containsString("Name and sex are required fields"))
                .log().all(); // Log the response for debugging
    }

    // Additional test methods can be added here following the same pattern

    /**
     * Helper method to retrieve a user by ID from the list of users.
     *
     * @param response The Response object from the GET request.
     * @param userId   The ID of the user to retrieve.
     * @return The JSONObject representing the user, or null if not found.
     */
    private JSONObject getUserById(Response response, Long userId) {
        String responseBody = response.getBody().asString();
        JSONArray usersArray = new JSONArray(responseBody);
        for (int i = 0; i < usersArray.length(); i++) {
            JSONObject user = usersArray.getJSONObject(i);
            if (user.getLong("id") == userId) {
                return user;
            }
        }
        return null;
    }

    /**
     * Attaches the response status code to the Allure report.
     *
     * @param response The Response object containing the status code.
     * @return The status code as a string.
     */
    @Attachment(value = "Response Status Code", type = "text/plain")
    public static String attachStatusCode(Response response) {
        return String.valueOf(response.getStatusCode());
    }

    /**
     * Attaches the response body to the Allure report.
     *
     * @param response The Response object containing the response body.
     * @return The response body as a string.
     */
    @Attachment(value = "Response Body", type = "text/plain")
    public static String attachResponseBody(Response response) {
        return response.getBody().asString();
    }

    /**
     * Helper method to attach both the status code and response body to Allure.
     *
     * @param response The Response object containing the status code and response body.
     */
    private static void attachResponseDetails(Response response) {
        attachStatusCode(response);
        attachResponseBody(response);
    }
}
