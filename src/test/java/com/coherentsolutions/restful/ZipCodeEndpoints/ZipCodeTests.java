package com.coherentsolutions.restful.ZipCodeEndpoints;

import com.coherentsolutions.restful.auth.OAuth2Client;
import com.coherentsolutions.restful.config.ApplicationConfig;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Zip Code Management")
public class ZipCodeTests {

    private static final Logger logger = LoggerFactory.getLogger(ZipCodeTests.class);

    // Configuration variables
    private static String BASE_URL;
    private static String TOKEN_ENDPOINT;
    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static RequestSpecification requestSpec;

    // Access token
    private static String accessToken;

    // Initial zip codes
    private final List<String> initialZipCodes = Arrays.asList("10001", "20002", "30003");

    @BeforeEach
    @Step("Global Setup: Configuring Rest-Assured and obtaining access token")
    void globalSetUp() {
        // Initialize ApplicationConfig
        ApplicationConfig config = ApplicationConfig.getInstance();

        // Retrieve configuration properties
        BASE_URL = config.getApiBaseUrl();
        TOKEN_ENDPOINT = config.getTokenUrl();
        CLIENT_ID = config.getClientId();
        CLIENT_SECRET = config.getClientSecret();

        // Validate that essential properties are not null or empty
        assertNotNull(BASE_URL, "API Base URL should not be null");
        assertNotNull(TOKEN_ENDPOINT, "Token URL should not be null");
        assertNotNull(CLIENT_ID, "Client ID should not be null");
        assertNotNull(CLIENT_SECRET, "Client Secret should not be null");
        assertFalse(BASE_URL.isEmpty(), "API Base URL should not be empty");
        assertFalse(TOKEN_ENDPOINT.isEmpty(), "Token URL should not be empty");
        assertFalse(CLIENT_ID.isEmpty(), "Client ID should not be empty");
        assertFalse(CLIENT_SECRET.isEmpty(), "Client Secret should not be empty");

        // Initialize Rest-Assured's request specification
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.URLENC) // OAuth2 token requests typically use application/x-www-form-urlencoded
                .log(LogDetail.ALL)
                .build();

        RestAssured.requestSpecification = requestSpec;

        logger.info("Rest-Assured configured with Base URL: {}", BASE_URL);
        logger.info("Token Endpoint: {}", TOKEN_ENDPOINT);
        attachCustomMessage("Rest-Assured configured successfully with Base URL: " + BASE_URL);

        // Obtain access token
        accessToken = obtainAccessToken("write"); // For example, obtaining write token for setup
        logger.info("Obtained Access Token for scope 'write': {}", accessToken);
        Allure.step("Obtained Access Token");
        attachToken("Access Token", accessToken);
    }

    @BeforeEach
    @Step("Test Setup: Resetting zip codes and cleaning up users")
    void setUp() {
        // Clean up existing users
        Allure.step("Cleaning up existing users");
        deleteAllUsers();

        // Reset zip codes to a known state before each test
        Allure.step("Resetting zip codes to initial state");
        resetZipCodes(initialZipCodes);
    }

    /**
     * Obtains an access token with the specified scope.
     *
     * @param scope The scope for which the token is requested.
     * @return The access token as a String.
     */
    @Step("Obtaining access token with scope: {scope}")
    private String obtainAccessToken(String scope) {
        try {
            Response tokenResponse = given()
                    .auth()
                    .preemptive()
                    .basic(CLIENT_ID, CLIENT_SECRET)
                    .formParam("grant_type", "client_credentials")
                    .formParam("scope", scope)
                    .when()
                    .post(TOKEN_ENDPOINT)
                    .then()
                    .extract()
                    .response();

            attachResponseDetails(tokenResponse);

            // Assert that the response status code is 200 (OK)
            assertEquals(200, tokenResponse.getStatusCode(),
                    "Expected status code 200 but got " + tokenResponse.getStatusCode());

            // Extract the access token from the response
            String token = tokenResponse.jsonPath().getString("access_token");

            // Log and attach the token
            logger.info("Obtained Access Token for scope '{}': {}", scope, token);
            Allure.step("Obtained Access Token for scope '" + scope + "': " + token);
            attachToken("Access Token (" + scope + ")", token);

            // Perform assertions on the token
            assertNotNull(token, "Token should not be null");
            assertFalse(token.isEmpty(), "Token should not be empty");
            assertTrue(token.length() > 0, "Token length should be greater than 0");

            return token;
        } catch (Exception e) {
            attachCustomMessage("Exception during obtainAccessToken: " + e.getMessage());
            throw e; // Re-throw to let the setup fail
        }
    }

    /**
     * Deletes all users by sending a DELETE request to /users/all
     */
    @Step("Deleting all users via API")
    private void deleteAllUsers() {
        try {
            Response deleteUsersResponse = given()
                    .auth()
                    .oauth2(accessToken)
                    .contentType(ContentType.JSON)
                    .when()
                    .delete("/users/all") // Corrected Path
                    .then()
                    .extract()
                    .response();

            attachResponseDetails(deleteUsersResponse);

            // Assert that the response status code is 200 (OK) or 204 (No Content)
            assertTrue(deleteUsersResponse.getStatusCode() == 200 || deleteUsersResponse.getStatusCode() == 204,
                    "Expected status code 200 or 204 but got " + deleteUsersResponse.getStatusCode());
        } catch (Exception e) {
            attachCustomMessage("Exception during deleteAllUsers: " + e.getMessage());
            throw e; // Re-throw to let the test fail
        }
    }

    /**
     * Resets zip codes by sending a POST request to /zip-codes/reset with the initial zip codes
     *
     * @param zipCodes List of zip codes to set as initial
     */
    @Step("Resetting zip codes via API")
    private void resetZipCodes(List<String> zipCodes) {
        try {
            Response resetZipCodesResponse = given()
                    .auth()
                    .oauth2(accessToken)
                    .contentType(ContentType.JSON)
                    .body(zipCodes)
                    .when()
                    .post("/zip-codes/reset")
                    .then()
                    .extract()
                    .response();

            attachResponseDetails(resetZipCodesResponse);

            // Assert that the response status code is 200 (OK) and message is correct
            assertEquals(200, resetZipCodesResponse.getStatusCode(),
                    "Expected status code 200 but got " + resetZipCodesResponse.getStatusCode());
            assertEquals("Zip codes reset successfully", resetZipCodesResponse.getBody().asString(),
                    "Unexpected response body: " + resetZipCodesResponse.getBody().asString());
        } catch (Exception e) {
            attachCustomMessage("Exception during resetZipCodes: " + e.getMessage());
            throw e; // Re-throw to let the test fail
        }
    }

    @Test
    @Order(1)
    @Story("Retrieve Available Zip Codes")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Ensure that the API retrieves all available zip codes correctly.")
    public void testGetAvailableZipCodes() {
        logger.info("Running Scenario #1: Retrieve Available Zip Codes");

        Allure.step("Sending GET request to retrieve available zip codes");
        Response getZipCodesResponse = given()
                .auth()
                .oauth2(accessToken)
                .contentType(ContentType.JSON)
                .when()
                .get("/zip-codes")
                .then()
                .extract()
                .response();

        attachResponseDetails(getZipCodesResponse);

        // Assert that the response status code is 200 (OK)
        assertEquals(200, getZipCodesResponse.getStatusCode(),
                "Expected status code 200 but got " + getZipCodesResponse.getStatusCode());

        // Extract the list of zip codes from the response
        List<String> retrievedZipCodes = getZipCodesResponse.jsonPath().getList("code");

        // Log and attach the retrieved zip codes
        logger.info("Retrieved zip codes: {}", retrievedZipCodes);
        Allure.step("Retrieved zip codes: " + retrievedZipCodes);
        attachCustomMessage("Retrieved zip codes: " + retrievedZipCodes);

        // Assert that the retrieved zip codes contain all initial zip codes
        assertNotNull(retrievedZipCodes, "Zip codes should not be null");
        assertFalse(retrievedZipCodes.isEmpty(), "Zip codes should not be empty");
        assertTrue(retrievedZipCodes.containsAll(initialZipCodes),
                "Retrieved zip codes should contain all initial zip codes");
        assertEquals(initialZipCodes.size(), retrievedZipCodes.size(),
                "Number of retrieved zip codes should match initial count");
    }

    @Test
    @Order(2)
    @Story("Add New Zip Codes")
    @Severity(SeverityLevel.NORMAL)
    @Description("Ensure that new zip codes can be added successfully.")
    public void testAddZipCodes() throws IOException {
        logger.info("Running Scenario #2: Add New Zip Codes");

        List<String> newZipCodes = Arrays.asList("40004", "50005");
        String stepDescription = "Adding new zip codes: " + newZipCodes;
        Allure.step(stepDescription);
        OAuth2Client.getInstance().addZipCodes(newZipCodes);

        Allure.step("Retrieving updated list of zip codes");
        List<String> updatedZipCodes = OAuth2Client.getInstance().getAvailableZipCodes();
        logger.info("Updated zip codes after addition: {}", updatedZipCodes);

        Allure.step("Verifying that new zip codes are added correctly");
        assertTrue(updatedZipCodes.containsAll(initialZipCodes), "Updated zip codes should contain all initial zip codes");
        assertTrue(updatedZipCodes.containsAll(newZipCodes), "Updated zip codes should contain all new zip codes");
        assertEquals(initialZipCodes.size() + newZipCodes.size(), updatedZipCodes.size(),
                "Total number of zip codes should be sum of initial and new");
    }

    @Test
    @Order(3)
    @Story("Add Duplicate Zip Codes")
    @Severity(SeverityLevel.MINOR)
    @Description("Ensure that adding duplicate zip codes is handled properly without duplication.")
    public void testAddDuplicateZipCodes() throws IOException {
        logger.info("Running Scenario #3: Add Duplicate Zip Codes");

        List<String> duplicateZipCodes = Arrays.asList("40004", "10001", "40004");
        String stepDescription = "Adding zip codes with duplicates: " + duplicateZipCodes;
        Allure.step(stepDescription);
        OAuth2Client.getInstance().addZipCodes(duplicateZipCodes);

        Allure.step("Retrieving updated list of zip codes after attempting to add duplicates");
        List<String> updatedZipCodes = OAuth2Client.getInstance().getAvailableZipCodes();
        logger.info("Updated zip codes after adding duplicates: {}", updatedZipCodes);

        Allure.step("Verifying that duplicates are not added");
        assertTrue(updatedZipCodes.containsAll(initialZipCodes), "Updated zip codes should contain all initial zip codes");
        assertTrue(updatedZipCodes.contains("40004"), "Updated zip codes should contain the new unique zip code");
        assertEquals(initialZipCodes.size() + 1, updatedZipCodes.size(),
                "Total number of zip codes should increase by 1 due to unique addition");
    }

    /**
     * Attaches the entire response body to the Allure report.
     *
     * @param response The Response object to attach.
     */
    @Attachment(value = "API Response", type = "application/json")
    private String attachResponseDetails(Response response) {
        return response.getBody().asPrettyString();
    }

    /**
     * Attaches the token to the Allure report.
     *
     * @param tokenName The name of the token (e.g., "Access Token").
     * @param token     The token string to attach.
     */
    @Attachment(value = "{tokenName}", type = "text/plain")
    private String attachToken(String tokenName, String token) {
        return token;
    }

    /**
     * Attaches a custom message to the Allure report.
     *
     * @param message The message to attach.
     */
    @Attachment(value = "Custom Message", type = "text/plain")
    private String attachCustomMessage(String message) {
        return message;
    }
}
