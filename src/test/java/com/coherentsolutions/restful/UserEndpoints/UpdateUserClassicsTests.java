package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.config.RestAssuredConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Update User")
public class UpdateUserClassicsTests extends RestAssuredConfig {

    private static final Logger logger = LoggerFactory.getLogger(UpdateUserClassicsTests.class);
    private static String writeToken;
    private static Long testUserId;

    @BeforeAll
    @Step("Global setup: Initializing services and creating a test user")
    static void setUp() {
        writeToken = getOAuthToken("write");

        // Reset zip codes to a known state
        Allure.step("Resetting zip codes to default values");
        given()
                .auth().oauth2(writeToken)
                .body(Arrays.asList("10001", "20002", "30003"))
                .when()
                .post(API_BASE_URL + "/zip-codes/reset")
                .then()
                .statusCode(200);

        // Create a user to update
        Allure.step("Creating a test user: TestUser");
        String userJson = """
            {
                "name": "TestUser",
                "email": "testuser@example.com",
                "sex": "Male",
                "age": 25,
                "zipCode": "10001"
            }""";

        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .post(API_BASE_URL + "/users");

        attachResponseDetails(response);
        response.then().statusCode(201);

        // Parse response body to get user ID
        JSONObject jsonResponse = new JSONObject(response.getBody().asString());
        testUserId = jsonResponse.getLong("id");
    }

    @Test
    @Order(1)
    @Story("Successful User Update")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that a user can be updated successfully with valid data.")
    public void testUpdateUser_Success() {
        logger.info("Running Scenario #1: Update user successfully");

        String userJson = """
            {
                "name": "UpdatedName",
                "email": "updated@example.com",
                "sex": "Female",
                "age": 28,
                "zipCode": "10001"
            }""";

        // Start Allure step
        Allure.step("Sending PUT request to update user: " + testUserId);

        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .put(API_BASE_URL + "/users/" + testUserId);

        attachResponseDetails(response);

        response.then().statusCode(200);

        // Optionally, verify the updated details
        Allure.step("Verifying the updated user details");

        Response getResponse = given()
                .auth().oauth2(writeToken)
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseDetails(getResponse);

        getResponse.then()
                .statusCode(200)
                .body("find { it.id == %d }.name", withArgs(testUserId), equalTo("UpdatedName"))
                .body("find { it.id == %d }.email", withArgs(testUserId), equalTo("updated@example.com"))
                .body("find { it.id == %d }.sex", withArgs(testUserId), equalTo("Female"))
                .body("find { it.id == %d }.age", withArgs(testUserId), equalTo(28));
    }

    // Adjust other tests similarly

    @Test
    @Order(2)
    @Story("Update User with Invalid Zip Code")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to update a user with an invalid zip code to ensure proper error handling.")
    public void testUpdateUser_InvalidZipCode() {
        logger.info("Running Scenario #2: Update user with invalid zip code");

        String userJson = """
            {
                "name": "UpdatedName",
                "email": "updated@example.com",
                "sex": "Female",
                "age": 28,
                "zipCode": "99999"
            }""";

        // Start Allure step
        Allure.step("Sending PUT request to update user: " + testUserId + " with invalid zip code 99999");

        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .put(API_BASE_URL + "/users/" + testUserId);

        attachResponseDetails(response);

        response.then()
                .statusCode(424)
                .body(containsString("Zip code is unavailable"));
    }

    @Test
    @Order(3)
    @Story("Update User with Missing Required Fields")
    @Severity(SeverityLevel.NORMAL)
    @Description("Attempt to update a user without providing all required fields to ensure proper validation.")
    public void testUpdateUser_MissingRequiredFields() {
        logger.info("Running Scenario #3: Update user with missing required fields");

        String userJson = """
            {
                "email": "updated@example.com",
                "age": 28
            }""";

        // Start Allure step
        Allure.step("Sending PUT request to update user: " + testUserId + " with missing required fields");

        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .put(API_BASE_URL + "/users/" + testUserId);

        attachResponseDetails(response);

        response.then()
                .statusCode(400)
                .body(containsString("Name and sex are required fields"));
    }

    // Helper methods for Allure attachments
    @Attachment(value = "Response Status Code", type = "text/plain")
    public static String attachStatusCode(Response response) {
        return String.valueOf(response.getStatusCode());
    }

    @Attachment(value = "Response Body", type = "text/plain")
    public static String attachResponseBody(Response response) {
        return response.getBody().asString();
    }

    private static void attachResponseDetails(Response response) {
        attachStatusCode(response);
        attachResponseBody(response);
    }
}
