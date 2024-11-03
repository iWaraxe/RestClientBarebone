package com.coherentsolutions.restful.utils;

import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.core5.http.HttpEntity;


/**
 * A builder class for constructing HTTP requests using the Builder Pattern.
 */
public class HttpRequestBuilder {
    private HttpUriRequestBase request;

    /**
     * Initializes the builder with the specified HTTP method and URL.
     *
     * @param method The HTTP method (e.g., "GET", "POST", "PUT", "DELETE").
     * @param url    The target URL for the HTTP request.
     * @throws IllegalArgumentException If the HTTP method is unsupported.
     */
    public HttpRequestBuilder(String method, String url) {
        switch (method.toUpperCase()) {
            case "GET":
                request = new HttpGet(url);
                break;
            case "POST":
                request = new HttpPost(url);
                break;
            case "PUT":
                request = new HttpPut(url);
                break;
            case "DELETE":
                request = new HttpDelete(url);
                break;
            case "PATCH":
                request = new HttpPatch(url);
                break;
            case "HEAD":
                request = new HttpHead(url);
                break;
            case "OPTIONS":
                request = new HttpOptions(url);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }

    /**
     * Adds a header to the HTTP request.
     *
     * @param name  The name of the header.
     * @param value The value of the header.
     * @return The current instance of HttpRequestBuilder.
     */
    public HttpRequestBuilder addHeader(String name, String value) {
        request.addHeader(name, value);
        return this;
    }

    /**
     * Sets the entity (body) of the HTTP request.
     *
     * @param entity The HttpEntity to set as the request body.
     * @return The current instance of HttpRequestBuilder.
     * @throws UnsupportedOperationException If the HTTP method does not support an entity.
     */
    public HttpRequestBuilder setEntity(HttpEntity entity) {
        request.setEntity(entity);
        return this;
    }

    /**
     * Builds and returns the constructed HTTP request.
     *
     * @return The constructed HttpUriRequestBase instance.
     */
    public HttpUriRequestBase build() {
        return request;
    }
}
