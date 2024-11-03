package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.config.RestAssuredConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Delete User")
public class DeleteUserTests extends RestAssuredConfig {

    private static final Logger logger = LoggerFactory.getLogger(DeleteUserTests.class);
    private String writeToken;

    @BeforeEach
    @Step("Setting up the test environment")
    void setUp() {
        writeToken = getOAuthToken("write");

        // Reset zip codes
        Allure.step("Resetting zip codes to default");
        given()
                .auth().oauth2(writeToken)
                .body(Arrays.asList("10001", "20002", "30003"))
                .when()
                .post(API_BASE_URL + "/zip-codes/reset")
                .then()
                .statusCode(200);

        // Clean up users
        Allure.step("Cleaning up existing users");
        given()
                .auth().oauth2(writeToken)
                .when()
                .delete(API_BASE_URL + "/users/all")
                .then()
                .statusCode(204);

        // Create test users
        Allure.step("Creating test users: Alice and Bob");
        createTestUsers();
    }

    private void createTestUsers() {
        // Create Alice
        String aliceJson = """
            {
                "name": "Alice",
                "email": "alice@example.com",
                "sex": "Female",
                "age": 25,
                "zipCode": {
                    "code": "10001"
                }
            }""";

        given()
                .auth().oauth2(writeToken)
                .body(aliceJson)
                .when()
                .post(API_BASE_URL + "/users")
                .then()
                .statusCode(201);

        // Create Bob
        String bobJson = """
            {
                "name": "Bob",
                "email": "bob@example.com",
                "sex": "Male",
                "age": 30,
                "zipCode": {
                    "code": "20002"
                }
            }""";

        given()
                .auth().oauth2(writeToken)
                .body(bobJson)
                .when()
                .post(API_BASE_URL + "/users")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(1)
    @Story("Delete Existing User with All Fields")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that an existing user can be deleted successfully when all fields are provided.")
    public void testDeleteUser_AllFieldsProvided() {
        logger.info("Running Scenario #1: Delete user with all fields provided");

        String userToDelete = """
            {
                "name": "Alice",
                "sex": "Female",
                "email": "alice@example.com",
                "age": 25,
                "zipCode": "10001"
            }""";

        Allure.step("Deleting user: Alice with all provided fields");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userToDelete)
                .when()
                .delete(API_BASE_URL + "/users");

        // Attach response details to Allure
        attachResponseToAllure(response);

        response.then().statusCode(204);

        // Verify user is deleted
        Allure.step("Verifying that user Alice is deleted");
        given()
                .auth().oauth2(writeToken)
                .when()
                .get(API_BASE_URL + "/users")
                .then()
                .statusCode(200)
                .body("findAll { it.name == 'Alice' }", empty());

        // Verify zip code is returned to available list
        Allure.step("Verifying that zip code 10001 is available");
        given()
                .auth().oauth2(writeToken)
                .when()
                .get(API_BASE_URL + "/zip-codes")
                .then()
                .statusCode(200)
                .body("findAll { it.code == '10001' }", not(empty()));
    }

    @Test
    @Order(2)
    @Story("Delete Existing User with Required Fields Only")
    @Severity(SeverityLevel.NORMAL)
    @Description("Ensure that an existing user can be deleted successfully when only required fields are provided.")
    public void testDeleteUser_RequiredFieldsOnly() {
        logger.info("Running Scenario #2: Delete user with required fields only");

        String userToDelete = """
            {
                "name": "Bob",
                "sex": "Male"
            }""";

        Allure.step("Deleting user: Bob with required fields only");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userToDelete)
                .when()
                .delete(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then().statusCode(204);

        // Verify user deletion
        verifyUserDeletion("Bob", "20002");
    }

    @Test
    @Order(3)
    @Story("Attempt to Delete User with Missing Required Fields")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to delete a user without providing all required fields to ensure proper error handling.")
    public void testDeleteUser_MissingRequiredFields() {
        logger.info("Running Scenario #3: Delete user with missing required fields");

        String userToDelete = """
            {
                "name": "Charlie"
            }""";

        Allure.step("Attempting to delete user: Charlie with missing required fields");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userToDelete)
                .when()
                .delete(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(409)
                .body(containsString("Name and sex are required fields"));
    }

    @Test
    @Order(4)
    @Story("Delete Non-Existent User")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Attempt to delete a user that does not exist to ensure proper error handling.")
    public void testDeleteUser_NonExistentUser() {
        logger.info("Running Scenario #4: Delete a non-existent user");

        String userToDelete = """
            {
                "name": "NonExistentUser",
                "sex": "Female"
            }""";

        Allure.step("Attempting to delete non-existent user: NonExistentUser");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userToDelete)
                .when()
                .delete(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(409)
                .body(containsString("User not found"));
    }

    @Test
    @Order(5)
    @Story("Unauthorized Deletion of User")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Attempt to delete a user with invalid tokens to ensure unauthorized access is handled correctly.")
    public void testDeleteUser_UnauthorizedAccess() {
        logger.info("Running Scenario #5: Unauthorized access to delete user");

        String userToDelete = """
            {
                "name": "Alice",
                "sex": "Female"
            }""";

        Allure.step("Attempting to delete user with invalid token");
        Response response = given()
                .auth().oauth2("invalid_token")
                .body(userToDelete)
                .when()
                .delete(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then().statusCode(401);
    }

    @Test
    @Order(6)
    @Story("Delete User with Invalid Token")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Attempt to delete a user using an explicitly invalid token to ensure unauthorized access is handled correctly.")
    public void testDeleteUser_InvalidToken() {
        logger.info("Running Scenario #6: Delete user with invalid token");

        String userToDelete = """
            {
                "name": "Bob",
                "sex": "Male"
            }""";

        Allure.step("Attempting to delete user with explicitly invalid token");
        Response response = given()
                .auth().oauth2("explicitly_invalid_token")
                .body(userToDelete)
                .when()
                .delete(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then().statusCode(401);
    }

    @Test
    @Order(7)
    @Story("Attempt to Use Invalid HTTP Method")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to use an unsupported HTTP method to delete a user and ensure proper error handling.")
    public void testDeleteUser_InvalidMethod() {
        logger.info("Running Scenario #7: Invalid HTTP method for delete user");

        Allure.step("Attempting to use DELETE method on a GET-only endpoint");
        Response response = given()
                .auth().oauth2(writeToken)
                .when()
                .delete(API_BASE_URL + "/users/available");

        attachResponseToAllure(response);

        response.then().statusCode(405);
    }

    private void verifyUserDeletion(String userName, String zipCode) {
        // Verify user is deleted
        given()
                .auth().oauth2(writeToken)
                .when()
                .get(API_BASE_URL + "/users")
                .then()
                .statusCode(200)
                .body("findAll { it.name == '" + userName + "' }", empty());

        // Verify zip code is available
        given()
                .auth().oauth2(writeToken)
                .when()
                .get(API_BASE_URL + "/zip-codes")
                .then()
                .statusCode(200)
                .body("findAll { it.code == '" + zipCode + "' }", not(empty()));
    }

    @Attachment(value = "Response Status Code", type = "text/plain")
    private String attachStatusCode(Response response) {
        return String.valueOf(response.getStatusCode());
    }

    @Attachment(value = "Response Body", type = "text/plain")
    private String attachResponseBody(Response response) {
        return response.getBody().asString();
    }

    private void attachResponseToAllure(Response response) {
        attachStatusCode(response);
        attachResponseBody(response);
    }
}