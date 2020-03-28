package com.starglare.accasy.core;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpClient {
    private String url;
    private String body;
    private final OkHttpClient httpClient;
    private MediaType mediaType;
    private Request request;
    public static int TIMEOUT = 5;

    public static final String JSONMediaType = "application/json; charset=utf-8";

    public HttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public void url(String url) {
        this.url = url;
    }

    public void body(String body) {
        this.body = body;
    }

    public void mediaType(String requestType) {
        this.mediaType = MediaType.get(requestType);
    }


    public void buildRequest() {
        request = new Request.Builder()
                .url(url)
                .post(getRequestBody())
                .build();
    }

    public Call newRequest() {
        return httpClient.newCall(request);
    }

    private RequestBody getRequestBody() {
        return RequestBody.create(mediaType, body);
    }
}
