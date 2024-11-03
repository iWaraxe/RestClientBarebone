package com.coherentsolutions.restful.auth;

import java.io.IOException;

// IOAuth2Client.java
public interface IOAuth2Client {
    String getReadToken() throws IOException;
    String getWriteToken() throws IOException;
}