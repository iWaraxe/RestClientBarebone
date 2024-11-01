package com.coherentsolutions.restful.client;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import java.io.IOException;

// HttpClientComponent.java
public interface HttpClientComponent {
    CloseableHttpResponse execute(HttpUriRequestBase request) throws IOException;
}
