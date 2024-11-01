package com.coherentsolutions.restful.client;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import java.io.IOException;

// HttpClientDecorator.java
public abstract class HttpClientDecorator implements HttpClientComponent {
    protected final HttpClientComponent decoratedClient;

    public HttpClientDecorator(HttpClientComponent decoratedClient) {
        this.decoratedClient = decoratedClient;
    }

    @Override
    public CloseableHttpResponse execute(HttpUriRequestBase request) throws IOException {
        return decoratedClient.execute(request);
    }
}

