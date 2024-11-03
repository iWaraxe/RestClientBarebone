package com.coherentsolutions.restful.client;

import com.coherentsolutions.restful.ApiResponse;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.io.IOException;

// IHttpClient.java
public interface IHttpClient {
    ApiResponse execute(HttpUriRequestBase request) throws IOException;
}