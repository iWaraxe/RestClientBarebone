package com.coherentsolutions.restful.client;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;

// BasicHttpClient.java
public class BasicHttpClient implements HttpClientComponent {
    private final CloseableHttpClient httpClient;

    public BasicHttpClient() {
        this.httpClient = HttpClients.createDefault();
    }

    @Override
    public CloseableHttpResponse execute(HttpUriRequestBase request) throws IOException {
        return httpClient.execute(request);
    }
}
