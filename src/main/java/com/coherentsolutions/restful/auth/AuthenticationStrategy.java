package com.coherentsolutions.restful.auth;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.io.IOException;

// AuthenticationStrategy.java
public interface AuthenticationStrategy {
    void authenticate(HttpUriRequestBase request) throws IOException;
}
