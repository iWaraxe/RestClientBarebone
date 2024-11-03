package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.config.RestAssuredConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Create User")
public class PostUsersTests extends RestAssuredConfig {

    private static final Logger logger = LoggerFactory.getLogger(PostUsersTests.class);
    private String writeToken;

    @BeforeEach
    @Step("Setting up the test environment")
    void setUp() {
        writeToken = getOAuthToken("write");

        // Reset zip codes to known state
        Allure.step("Resetting zip codes to default");
        given()
                .auth().oauth2(writeToken)
                .body(Arrays.asList("10001", "20002", "30003"))
                .when()
                .post(API_BASE_URL + "/zip-codes/reset")
                .then()
                .statusCode(200);
    }

    @AfterEach
    @Step("Tearing down the test environment")
    void tearDown() {
        // Clean up users
        Allure.step("Cleaning up users after test execution");
        given()
                .auth().oauth2(writeToken)
                .when()
                .delete(API_BASE_URL + "/users/all")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(1)
    @Story("Create User with All Fields")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that a user can be created successfully when all required fields are provided and the zip code is valid.")
    public void testCreateUser_AllFieldsFilled_ValidZipCode() {
        logger.info("Running Scenario #1: All fields filled, valid zip code");

        String userJson = """
            {
                "name": "Alice",
                "email": "alice@example.com",
                "sex": "Female",
                "age": 25,
                "zipCode": {
                    "code": "10001"
                }
            }""";

        Allure.step("Sending POST request to create user: Alice with all fields filled");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .post(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(201)
                .body("name", equalTo("Alice"));

        // Verify that the zip code is now unavailable
        Allure.step("Verifying that zip code 10001 is unavailable after user creation");
        given()
                .auth().oauth2(writeToken)
                .when()
                .get(API_BASE_URL + "/zip-codes")
                .then()
                .statusCode(200)
                .body("findAll { it.code == '10001' }.available", not(hasItem(true)));
    }

    @Test
    @Order(2)
    @Story("Create User with Required Fields Only")
    @Severity(SeverityLevel.NORMAL)
    @Description("Ensure that a user can be created successfully when only required fields are provided.")
    public void testCreateUser_RequiredFieldsOnly() {
        logger.info("Running Scenario #2: Required fields only");

        String userJson = """
            {
                "name": "Bob",
                "sex": "Male"
            }""";

        Allure.step("Sending POST request to create user: Bob with required fields only");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .post(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(201)
                .body("name", equalTo("Bob"));
    }

    @Test
    @Order(3)
    @Story("Create User with Invalid Zip Code")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to create a user with an invalid zip code to ensure proper error handling.")
    public void testCreateUser_InvalidZipCode() {
        logger.info("Running Scenario #3: Invalid (unavailable) zip code");

        String userJson = """
            {
                "name": "Charlie",
                "email": "charlie@example.com",
                "sex": "Male",
                "zipCode": {
                    "code": "99999"
                }
            }""";

        Allure.step("Sending POST request to create user: Charlie with invalid zip code 99999");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .post(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(424)
                .body(containsString("Zip code is unavailable"));
    }

    @Test
    @Order(4)
    @Story("Create Duplicate User")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Attempt to create a duplicate user with the same name and sex to ensure proper error handling.")
    public void testCreateUser_DuplicateNameAndSex() {
        logger.info("Running Scenario #4: Duplicate name and sex");

        String userJson = """
            {
                "name": "Dana",
                "sex": "Female"
            }""";

        // Create initial user
        Allure.step("Creating initial user: Dana");
        Response response1 = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .post(API_BASE_URL + "/users");

        attachResponseToAllure(response1);
        response1.then().statusCode(201);

        // Attempt to create duplicate user
        Allure.step("Attempting to create duplicate user: Dana");
        Response response2 = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .post(API_BASE_URL + "/users");

        attachResponseToAllure(response2);
        response2.then()
                .statusCode(409)
                .body(containsString("already exists"));
    }

    @Test
    @Order(5)
    @Story("Create User with Empty Request Body")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to create a user with an empty request body to ensure proper error handling.")
    public void testCreateUser_EmptyRequestBody() {
        logger.info("Running Scenario #5: Empty request body");

        Allure.step("Sending POST request with empty request body");
        Response response = given()
                .auth().oauth2(writeToken)
                .body("{}")
                .when()
                .post(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(400)
                .body(containsString("required fields"));
    }

    @Test
    @Order(6)
    @Story("Create User with Invalid Email Format")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to create a user with an invalid email format to ensure proper validation.")
    public void testCreateUser_InvalidEmailFormat() {
        logger.info("Running Scenario #6: Invalid email format");

        String userJson = """
            {
                "name": "Eve",
                "email": "not-an-email",
                "sex": "Female"
            }""";

        Allure.step("Sending POST request to create user: Eve with invalid email format");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .post(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(400)
                .body(containsString("Invalid email format"));
    }

    @Test
    @Order(7)
    @Story("Create User with Missing Name Field")
    @Severity(SeverityLevel.NORMAL)
    @Description("Attempt to create a user without the name field to ensure proper validation.")
    public void testCreateUser_MissingNameField() {
        logger.info("Running Scenario #7: Missing name field");

        String userJson = """
            {
                "sex": "Male"
            }""";

        Allure.step("Sending POST request to create user with missing name field");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .post(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(400)
                .body(containsString("Name and sex are required fields"));
    }

    @Test
    @Order(8)
    @Story("Create User with Large Payload")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to create a user with a large payload to test API's handling of oversized requests.")
    public void testCreateUser_LargePayload() {
        logger.info("Running Scenario #8: Large payload");

        String largeName = new String(new char[10000]).replace("\0", "A");
        String userJson = String.format("""
            {
                "name": "%s",
                "email": "large@example.com",
                "sex": "Male"
            }""", largeName);

        Allure.step("Sending POST request to create user with large payload");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .post(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(anyOf(is(201), is(400)));
    }

    @Test
    @Order(9)
    @Story("Create User with Invalid HTTP Method")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to use an unsupported HTTP method to create a user and ensure proper error handling.")
    public void testCreateUser_InvalidHttpMethod() {
        logger.info("Running Scenario #9: Invalid HTTP method");

        String userJson = """
            {
                "name": "TestUser",
                "sex": "Male"
            }""";

        Allure.step("Sending request using invalid HTTP method (PATCH)");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .patch(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(405)
                .body(containsString("Method not allowed"));
    }

    @Test
    @Order(10)
    @Story("Create User with Special Characters in Name")
    @Severity(SeverityLevel.MINOR)
    @Description("Attempt to create a user with special characters in the name to test input sanitization.")
    public void testCreateUser_SpecialCharactersInName() {
        logger.info("Running Scenario #10: Special characters in name");

        String userJson = """
            {
                "name": "<script>alert('XSS')</script>",
                "email": "special@example.com",
                "sex": "Male"
            }""";

        Allure.step("Sending POST request to create user with special characters in name");
        Response response = given()
                .auth().oauth2(writeToken)
                .body(userJson)
                .when()
                .post(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(anyOf(is(201), is(400)));
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