package com.coherentsolutions.restful;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.io.IOException;

// RequestStrategy.java
public interface RequestStrategy {
    ApiResponse execute(HttpUriRequestBase request) throws IOException;
}

