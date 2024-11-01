package com.coherentsolutions.restful.ZipCodeEndpoints;

import com.coherentsolutions.restful.BearerTokenAuthentication;
import com.coherentsolutions.restful.OAuth2Client;
import com.coherentsolutions.restful.UserService;
import io.qameta.allure.*;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("User Management")
@Feature("Zip Code Management")
public class ZipCodeTests {

    private static final Logger logger = LoggerFactory.getLogger(ZipCodeTests.class);

    private OAuth2Client client;
    private UserService userService; // Added UserService instance
    private List<String> initialZipCodes = Arrays.asList("10001", "20002", "30003");

    @BeforeEach
    @Step("Setup: Initializing OAuth2 client, resetting zip codes, and cleaning up existing users")
    void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        userService = new UserService(new BearerTokenAuthentication(client)); // Initialize UserService

        // Clean up existing users using UserService
        Allure.step("Cleaning up existing users");
        userService.deleteAllUsers(); // Corrected method call

        // Reset zip codes to a known state before each test
        Allure.step("Resetting zip codes to default values");
        client.resetZipCodes(initialZipCodes);
    }

    @Test
    @Order(1)
    @Story("Retrieve Available Zip Codes")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Ensure that the API retrieves all available zip codes correctly.")
    public void testGetAvailableZipCodes() throws IOException {
        logger.info("Running Scenario #1: Retrieve Available Zip Codes");

        Allure.step("Sending GET request to retrieve available zip codes");
        List<String> zipCodes = client.getAvailableZipCodes();

        Allure.step("Verifying the retrieved zip codes");
        logger.info("Retrieved zip codes: {}", zipCodes);
        assertNotNull(zipCodes, "Zip codes should not be null");
        assertFalse(zipCodes.isEmpty(), "Zip codes should not be empty");
        assertTrue(zipCodes.containsAll(initialZipCodes), "Retrieved zip codes should contain all initial zip codes");
        assertEquals(initialZipCodes.size(), zipCodes.size(), "Number of retrieved zip codes should match initial count");
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
        client.addZipCodes(newZipCodes);

        Allure.step("Retrieving updated list of zip codes");
        List<String> updatedZipCodes = client.getAvailableZipCodes();
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
        client.addZipCodes(duplicateZipCodes);

        Allure.step("Retrieving updated list of zip codes after attempting to add duplicates");
        List<String> updatedZipCodes = client.getAvailableZipCodes();
        logger.info("Updated zip codes after adding duplicates: {}", updatedZipCodes);

        Allure.step("Verifying that duplicates are not added");
        assertTrue(updatedZipCodes.containsAll(initialZipCodes), "Updated zip codes should contain all initial zip codes");
        assertTrue(updatedZipCodes.contains("40004"), "Updated zip codes should contain the new unique zip code");
        assertEquals(initialZipCodes.size() + 1, updatedZipCodes.size(),
                "Total number of zip codes should increase by 1 due to unique addition");
    }

    // Optional: If you have methods that return ApiResponse or similar, you can attach their details as follows.
    // For current methods, zip codes are retrieved as List<String>, so attachments are not directly applicable.
    // However, you can still attach custom messages or logs for better traceability.

    /**
     * Attaches a custom message to the Allure report.
     *
     * @param message The message to attach.
     */
    @Attachment(value = "Custom Message", type = "text/plain")
    private String attachCustomMessage(String message) {
        return message;
    }

    /**
     * Helper method to log and attach custom messages.
     *
     * @param message The message to log and attach.
     */
    private void logAndAttach(String message) {
        logger.info(message);
        attachCustomMessage(message);
    }
}
