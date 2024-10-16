package com.coherentsolutions.restful;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OAuth2ClientTest {

    @Test
    void testGetReadToken() throws IOException {
        OAuth2Client client = OAuth2Client.getInstance();
        String token = client.getReadToken();
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testGetWriteToken() throws IOException {
        OAuth2Client client = OAuth2Client.getInstance();
        String token = client.getWriteToken();
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testSingletonInstance() {
        OAuth2Client instance1 = OAuth2Client.getInstance();
        OAuth2Client instance2 = OAuth2Client.getInstance();
        assertSame(instance1, instance2);
    }
}