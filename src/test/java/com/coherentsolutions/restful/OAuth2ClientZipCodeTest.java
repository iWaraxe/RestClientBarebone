package com.coherentsolutions.restful;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OAuth2ClientZipCodeTest {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2ClientZipCodeTest.class);

    private OAuth2Client client;
    private List<String> initialZipCodes = Arrays.asList("10001", "20002", "30003");

    @BeforeEach
    void setUp() throws IOException {
        client = OAuth2Client.getInstance();
        logger.info("Setting up test with initial zip codes: {}", initialZipCodes);
        client.resetZipCodes(initialZipCodes);
    }

    @Test
    void testGetAvailableZipCodes() throws IOException {
        List<String> zipCodes = client.getAvailableZipCodes();
        logger.info("Retrieved zip codes: {}", zipCodes);
        assertNotNull(zipCodes, "Zip codes should not be null");
        assertFalse(zipCodes.isEmpty(), "Zip codes should not be empty");
        assertTrue(zipCodes.containsAll(initialZipCodes), "Retrieved zip codes should contain all initial zip codes");
        assertEquals(initialZipCodes.size(), zipCodes.size(), "Number of retrieved zip codes should match initial count");
    }

    @Test
    void testAddZipCodes() throws IOException {
        List<String> newZipCodes = Arrays.asList("40004", "50005");
        logger.info("Adding new zip codes: {}", newZipCodes);
        client.addZipCodes(newZipCodes);

        List<String> updatedZipCodes = client.getAvailableZipCodes();
        logger.info("Updated zip codes after addition: {}", updatedZipCodes);
        assertTrue(updatedZipCodes.containsAll(initialZipCodes), "Updated zip codes should contain all initial zip codes");
        assertTrue(updatedZipCodes.containsAll(newZipCodes), "Updated zip codes should contain all new zip codes");
        assertEquals(initialZipCodes.size() + newZipCodes.size(), updatedZipCodes.size(), "Total number of zip codes should be sum of initial and new");
    }

    @Test
    void testAddDuplicateZipCodes() throws IOException {
        List<String> newZipCodes = Arrays.asList("40004", "10001", "40004");
        logger.info("Adding zip codes with duplicates: {}", newZipCodes);
        client.addZipCodes(newZipCodes);

        List<String> updatedZipCodes = client.getAvailableZipCodes();
        logger.info("Updated zip codes after adding duplicates: {}", updatedZipCodes);
        assertTrue(updatedZipCodes.containsAll(initialZipCodes), "Updated zip codes should contain all initial zip codes");
        assertTrue(updatedZipCodes.contains("40004"), "Updated zip codes should contain the new unique zip code");
        assertEquals(initialZipCodes.size() + 1, updatedZipCodes.size(), "Total number of zip codes should increase by 1");
    }
}