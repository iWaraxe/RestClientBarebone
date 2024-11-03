package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.config.RestAssuredConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Get Users")
public class GetUsersTests extends RestAssuredConfig {

    private static final Logger logger = LoggerFactory.getLogger(GetUsersTests.class);
    private static String readToken;
    private static String writeToken;

    @BeforeAll
    @Step("Global setup before all tests")
    static void globalSetUp() {
        writeToken = getOAuthToken("write");
        readToken = getOAuthToken("read");

        // Reset zip codes
        Allure.step("Resetting zip codes to default");
        given()
                .auth().oauth2(writeToken)
                .body(java.util.Arrays.asList("10001", "20002", "30003"))
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
        Allure.step("Creating test users: Alice, Bob, and Charlie");
        createTestUsers();
    }

    private static void createTestUsers() {
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
                "age": 35,
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

        // Create Charlie
        String charlieJson = """
            {
                "name": "Charlie",
                "email": "charlie@example.com",
                "sex": "Male",
                "age": 30,
                "zipCode": {
                    "code": "30003"
                }
            }""";

        given()
                .auth().oauth2(writeToken)
                .body(charlieJson)
                .when()
                .post(API_BASE_URL + "/users")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(1)
    @Story("Get All Users")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Retrieve all users and verify the count and details.")
    public void testGetAllUsers() {
        logger.info("Running Scenario #1: Get all users");

        Allure.step("Sending GET request to retrieve all users");
        Response response = given()
                .auth().oauth2(readToken)
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(200)
                .body("size()", is(3))
                .body("name", hasItems("Alice", "Bob", "Charlie"));
    }

    @Test
    @Order(2)
    @Story("Get Users Older Than a Specific Age")
    @Severity(SeverityLevel.NORMAL)
    @Description("Retrieve users older than a specified age and verify the results.")
    public void testGetUsersOlderThan() {
        logger.info("Running Scenario #2: Get users older than a specific age");

        Allure.step("Sending GET request with query parameter olderThan=30");
        Response response = given()
                .auth().oauth2(readToken)
                .queryParam("olderThan", "30")
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", equalTo("Bob"))
                .body("[0].age", greaterThan(30));
    }

    @Test
    @Order(3)
    @Story("Get Users Younger Than a Specific Age")
    @Severity(SeverityLevel.NORMAL)
    @Description("Retrieve users younger than a specified age and verify the results.")
    public void testGetUsersYoungerThan() {
        logger.info("Running Scenario #3: Get users younger than a specific age");

        Allure.step("Sending GET request with query parameter youngerThan=30");
        Response response = given()
                .auth().oauth2(readToken)
                .queryParam("youngerThan", "30")
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].name", equalTo("Alice"))
                .body("[0].age", lessThan(30));
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
    public void testGetUsersBySex(String sex, int expectedUserCount) {
        logger.info("Running Parameterized Test for sex: " + sex);

        Allure.step("Sending GET request with query parameter sex=" + sex);
        Response response = given()
                .auth().oauth2(readToken)
                .queryParam("sex", sex)
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(200)
                .body("size()", is(expectedUserCount))
                .body("findAll { it.sex == '" + sex + "' }", hasSize(expectedUserCount));
    }

    @Test
    @Order(5)
    @Story("Get Users with Invalid Query Parameter")
    @Severity(SeverityLevel.MINOR)
    @Description("Send a GET request with an invalid query parameter and verify API behavior.")
    public void testGetUsersWithInvalidQueryParameter() {
        logger.info("Running Scenario #5: Get users with an invalid query parameter");

        Allure.step("Sending GET request with invalid query parameter invalidParam=test");
        Response response = given()
                .auth().oauth2(readToken)
                .queryParam("invalidParam", "test")
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(200)
                .body("size()", is(3));
    }

    @Test
    @Order(6)
    @Story("Get Users with Multiple Query Parameters")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Send a GET request with multiple query parameters and verify the filtered results.")
    public void testGetUsersWithMultipleParameters() {
        logger.info("Running Scenario #6: Get users with multiple query parameters");

        Allure.step("Sending GET request with query parameters olderThan=25 and sex=Male");
        Response response = given()
                .auth().oauth2(readToken)
                .queryParam("olderThan", "25")
                .queryParam("sex", "Male")
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(200)
                .body("size()", is(2))
                .body("findAll { it.age > 25 && it.sex == 'Male' }", hasSize(2))
                .body("name", hasItems("Bob", "Charlie"));
    }

    @Test
    @Order(7)
    @Story("Get Users with No Matching Criteria")
    @Severity(SeverityLevel.NORMAL)
    @Description("Send a GET request with criteria that match no users and verify the response.")
    public void testGetUsersWithNoMatchingCriteria() {
        logger.info("Running Scenario #7: Get users with criteria that match no users");

        Allure.step("Sending GET request with query parameter olderThan=100");
        Response response = given()
                .auth().oauth2(readToken)
                .queryParam("olderThan", "100")
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @Order(8)
    @Story("Get Users Unauthorized")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Attempt to retrieve users without proper authorization and verify the response.")
    public void testGetUsersUnauthorized() {
        logger.info("Running Scenario #8: Unauthorized access to get users");

        Allure.step("Sending unauthorized GET request to retrieve users");
        Response response = given()
                .auth().oauth2("invalid_token")
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(401);
    }

    @Test
    @Order(10)
    @Story("Get Users with Special Characters in Parameters")
    @Severity(SeverityLevel.MINOR)
    @Description("Send a GET request with special characters in query parameters and verify API handling.")
    public void testGetUsersWithSpecialCharactersInParameters() {
        logger.info("Running Scenario #10: Get users with special characters in query parameters");

        Allure.step("Sending GET request with special characters in query parameter");
        Response response = given()
                .auth().oauth2(readToken)
                .queryParam("sex", "Male&Female")
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(anyOf(is(200), is(400)));
    }

    @Test
    @Order(11)
    @Story("Get Users with Invalid Age Parameter")
    @Severity(SeverityLevel.MINOR)
    @Description("Send a GET request with a non-integer value for age parameter and verify API response.")
    public void testGetUsersWithInvalidAgeParameter() {
        logger.info("Running Scenario #11: Get users with invalid age parameter");

        Allure.step("Sending GET request with invalid age parameter olderThan=abc");
        Response response = given()
                .auth().oauth2(readToken)
                .queryParam("olderThan", "abc")
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseToAllure(response);

        response.then()
                .statusCode(400);
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