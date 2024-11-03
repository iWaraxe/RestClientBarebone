package com.coherentsolutions.restful.UserEndpoints;

import com.coherentsolutions.restful.config.RestAssuredConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Concurrent User Updates")
public class UpdateUserConcurrencyTests extends RestAssuredConfig {

    private static final Logger logger = LoggerFactory.getLogger(UpdateUserConcurrencyTests.class);
    private static String writeToken;
    private static Long testUserId;

    @BeforeAll
    @Step("Global setup: Initializing RestAssured and creating a test user")
    static void setUp() {
        writeToken = getOAuthToken("write");

        // Reset zip codes to a known state
        Allure.step("Resetting zip codes to default values");
        given()
                .auth().oauth2(writeToken)
                .contentType("application/json")
                .body(Arrays.asList("10001", "20002", "30003"))
                .when()
                .post(API_BASE_URL + "/zip-codes/reset")
                .then()
                .statusCode(200);

        // Create a user to update
        Allure.step("Creating a test user: ConcurrentUser");
        String userJson = """
            {
                "name": "ConcurrentUser",
                "email": "concurrent@example.com",
                "sex": "Male",
                "age": 25,
                "zipCode": "10001"
            }""";

        Response response = given()
                .auth().oauth2(writeToken)
                .contentType("application/json")
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
    @Story("Concurrent Updates to a Single User")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure that multiple concurrent updates to the same user are handled correctly by the API.")
    public void testConcurrentUpdates() throws InterruptedException {
        logger.info("Running Scenario #1: Concurrent updates to a single user");

        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Future<Response>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            futures.add(executorService.submit(() -> {
                try {
                    String updatedUserJson = String.format("""
                        {
                            "name": "UpdatedUser%d",
                            "email": "updated%d@example.com",
                            "sex": "Male",
                            "age": %d,
                            "zipCode": "10001"
                        }""", index, index, 30 + index);

                    // Start Allure step within each task
                    Allure.step(String.format("Thread %d: Sending PUT request to update user: %d with data: %s",
                            index, testUserId, updatedUserJson));

                    Response response = given()
                            .auth().oauth2(writeToken)
                            .contentType("application/json")
                            .body(updatedUserJson)
                            .when()
                            .put(API_BASE_URL + "/users/" + testUserId);

                    attachResponseDetails(response);

                    response.then().statusCode(200);

                    return response;
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await(); // Wait for all threads to finish
        executorService.shutdown();

        // Verify all updates were successful
        for (int i = 0; i < numberOfThreads; i++) {
            try {
                Response response = futures.get(i).get();
                assertEquals(200, response.getStatusCode(),
                        String.format("Thread %d: Expected status code 200 but got %d", i, response.getStatusCode()));
            } catch (ExecutionException e) {
                Allure.step("Exception occurred during concurrent update: " + e.getMessage());
                fail("Exception occurred during concurrent update: " + e.getMessage());
            }
        }

        // Optionally, verify the final state of the user
        Allure.step("Verifying the final state of the user after concurrent updates");
        Response getResponse = given()
                .auth().oauth2(writeToken)
                .when()
                .get(API_BASE_URL + "/users");

        attachResponseDetails(getResponse);

        getResponse.then()
                .statusCode(200)
                .body("find { it.id == " + testUserId + " }.name", anyOf(
                        equalTo("UpdatedUser0"),
                        equalTo("UpdatedUser1"),
                        equalTo("UpdatedUser2"),
                        equalTo("UpdatedUser3"),
                        equalTo("UpdatedUser4")
                ))
                .body("find { it.id == " + testUserId + " }.email", anyOf(
                        equalTo("updated0@example.com"),
                        equalTo("updated1@example.com"),
                        equalTo("updated2@example.com"),
                        equalTo("updated3@example.com"),
                        equalTo("updated4@example.com")
                ))
                .body("find { it.id == " + testUserId + " }.sex", equalTo("Male"))
                .body("find { it.id == " + testUserId + " }.age", anyOf(
                        equalTo(30),
                        equalTo(31),
                        equalTo(32),
                        equalTo(33),
                        equalTo(34)
                ));
    }

    // Additional Test Methods can be added here following the same pattern

    /**
     * Attaches response status code to the Allure report.
     *
     * @param response The Response object containing status code.
     * @return The status code as a string.
     */
    @Attachment(value = "Response Status Code", type = "text/plain")
    public static String attachStatusCode(Response response) {
        return String.valueOf(response.getStatusCode());
    }

    /**
     * Attaches response body to the Allure report.
     *
     * @param response The Response object containing response body.
     * @return The response body as a string.
     */
    @Attachment(value = "Response Body", type = "text/plain")
    public static String attachResponseBody(Response response) {
        return response.getBody().asString();
    }

    /**
     * Helper method to attach both status code and response body.
     *
     * @param response The Response object containing status code and response body.
     */
    private static void attachResponseDetails(Response response) {
        attachStatusCode(response);
        attachResponseBody(response);
    }
}
