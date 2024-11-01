package com.coherentsolutions.restful;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
