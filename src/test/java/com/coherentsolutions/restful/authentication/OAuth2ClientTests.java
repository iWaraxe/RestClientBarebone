package com.coherentsolutions.restful.authentication;

import com.coherentsolutions.restful.config.ApplicationConfig;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("Authentication")
@Feature("OAuth2 Client Tests")
public class OAuth2ClientTests {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2ClientTests.class);

    // Configuration variables
    private static String BASE_URL;
    private static String TOKEN_ENDPOINT;
    private static String CLIENT_ID;
    private static String CLIENT_SECRET;

    private static RequestSpecification requestSpec;

    @BeforeAll
    @Step("Setup: Configuring Rest-Assured with base specifications")
    static void globalSetUp() {
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
    }

    @Test
    @Order(1)
    @Story("Retrieve Read Token")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Ensure that the API retrieves a valid read token successfully.")
    public void testGetReadToken() {
        logger.info("Running Scenario #1: Retrieve Read Token");

        Allure.step("Sending request to get read token");

        // Send POST request to obtain the read token
        Response tokenResponse = given()
                .auth()
                .preemptive()
                .basic(CLIENT_ID, CLIENT_SECRET)
                .formParam("grant_type", "client_credentials")
                .formParam("scope", "read") // Assuming "read" scope is used for read token
                .when()
                .post(TOKEN_ENDPOINT)
                .then()
                .extract()
                .response();

        attachResponseDetails(tokenResponse);

        // Assert that the response status code is 200 (OK)
        assertEquals(200, tokenResponse.getStatusCode(), "Expected status code 200 but got " + tokenResponse.getStatusCode());

        // Extract the access token from the response
        String accessToken = tokenResponse.jsonPath().getString("access_token");

        // Log and attach the token
        logger.info("Obtained Read Token: {}", accessToken);
        Allure.step("Obtained Read Token: " + accessToken);
        attachToken("Read Token", accessToken);

        // Perform assertions on the token
        assertNotNull(accessToken, "Token should not be null");
        assertFalse(accessToken.isEmpty(), "Token should not be empty");
        assertTrue(accessToken.length() > 0, "Token length should be greater than 0");
    }

    @Test
    @Order(2)
    @Story("Retrieve Write Token")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Ensure that the API retrieves a valid write token successfully.")
    public void testGetWriteToken() {
        logger.info("Running Scenario #2: Retrieve Write Token");

        Allure.step("Sending request to get write token");

        // Send POST request to obtain the write token
        Response tokenResponse = given()
                .auth()
                .preemptive()
                .basic(CLIENT_ID, CLIENT_SECRET)
                .formParam("grant_type", "client_credentials")
                .formParam("scope", "write") // Assuming "write" scope is used for write token
                .when()
                .post(TOKEN_ENDPOINT)
                .then()
                .extract()
                .response();

        attachResponseDetails(tokenResponse);

        // Assert that the response status code is 200 (OK)
        assertEquals(200, tokenResponse.getStatusCode(), "Expected status code 200 but got " + tokenResponse.getStatusCode());

        // Extract the access token from the response
        String accessToken = tokenResponse.jsonPath().getString("access_token");

        // Log and attach the token
        logger.info("Obtained Write Token: {}", accessToken);
        Allure.step("Obtained Write Token: " + accessToken);
        attachToken("Write Token", accessToken);

        // Perform assertions on the token
        assertNotNull(accessToken, "Token should not be null");
        assertFalse(accessToken.isEmpty(), "Token should not be empty");
        assertTrue(accessToken.length() > 0, "Token length should be greater than 0");
    }

    /**
     * Attaches the entire response to the Allure report.
     *
     * @param response The Response object to attach.
     */
    @Attachment(value = "OAuth2 Token Response", type = "application/json")
    private String attachResponseDetails(Response response) {
        return response.getBody().asPrettyString();
    }

    /**
     * Attaches the token to the Allure report.
     *
     * @param tokenName The name of the token (e.g., "Read Token", "Write Token").
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
    private static String attachCustomMessage(String message) {
        return message;
    }
}
