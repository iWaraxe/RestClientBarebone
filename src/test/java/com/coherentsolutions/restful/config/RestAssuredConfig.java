package com.coherentsolutions.restful.config;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.BeforeAll;

public class RestAssuredConfig {
    protected static RequestSpecification requestSpec;
    protected static ResponseSpecification responseSpec;
    protected static final String BASE_URL = "http://localhost:8080";
    protected static final String TOKEN_URL = BASE_URL + "/oauth2/token";
    protected static final String API_BASE_URL = BASE_URL + "/api";

    @BeforeAll
    public static void setup() {
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();

        responseSpec = new ResponseSpecBuilder()
                .log(LogDetail.ALL)
                .build();

        RestAssured.requestSpecification = requestSpec;
        RestAssured.responseSpecification = responseSpec;
    }

    protected static String getOAuthToken(String scope) {
        return RestAssured.given()
                .auth()
                .preemptive()
                .basic("0oa157tvtugfFXEhU4x7", "X7eBCXqlFC7x-mjxG5H91IRv_Bqe1oq7ZwXNA8aq")
                .contentType(ContentType.URLENC)
                .formParam("grant_type", "client_credentials")
                .formParam("scope", scope)
                .when()
                .post(TOKEN_URL)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("access_token");
    }
}
