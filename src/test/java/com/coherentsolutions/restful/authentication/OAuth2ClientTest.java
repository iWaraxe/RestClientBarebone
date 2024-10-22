package com.coherentsolutions.restful.authentication;

import com.coherentsolutions.restful.OAuth2Client;
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
}