package com.coherentsolutions.restful.authentication;

import com.coherentsolutions.restful.auth.OAuth2Client;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("Authentication")
@Feature("OAuth2 Client Tests")
public class OAuth2ClientTests {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2ClientTests.class);

    private OAuth2Client client;

    @BeforeEach
    @Step("Setup: Initializing OAuth2Client instance")
    void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        logger.info("OAuth2Client instance initialized");
        attachCustomMessage("OAuth2Client instance initialized");
    }

    @Test
    @Order(1)
    @Story("Retrieve Read Token")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Ensure that the API retrieves a valid read token successfully.")
    public void testGetReadToken() throws IOException {
        logger.info("Running Scenario #1: Retrieve Read Token");

        Allure.step("Sending request to get read token");
        String token = client.getReadToken();
        attachToken("Read Token", token);

        Allure.step("Asserting that the read token is valid");
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");
        assertTrue(token.length() > 0, "Token length should be greater than 0");
    }

    @Test
    @Order(2)
    @Story("Retrieve Write Token")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Ensure that the API retrieves a valid write token successfully.")
    public void testGetWriteToken() throws IOException {
        logger.info("Running Scenario #2: Retrieve Write Token");

        Allure.step("Sending request to get write token");
        String token = client.getWriteToken();
        attachToken("Write Token", token);

        Allure.step("Asserting that the write token is valid");
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");
        assertTrue(token.length() > 0, "Token length should be greater than 0");
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
    private String attachCustomMessage(String message) {
        return message;
    }
}
