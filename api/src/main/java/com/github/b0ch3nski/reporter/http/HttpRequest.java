package com.github.b0ch3nski.reporter.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public final class HttpRequest {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final int TIMEOUT_MILLIS = 2000;
    private final HttpURLConnection connection;

    private HttpRequest(String url, String method) throws IOException {
        connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("Accept-Charset", CHARSET.name());
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TIMEOUT_MILLIS);
        connection.setReadTimeout(TIMEOUT_MILLIS);
    }

    public static HttpRequest get(String url) throws IOException {
        return new HttpRequest(url, "GET");
    }

    public static HttpRequest post(String url) throws IOException {
        return new HttpRequest(url, "POST");
    }

    public HttpRequest withPayload(String payload) throws IOException {
        byte[] payloadBytes = payload.getBytes(CHARSET);

        connection.setRequestProperty("Content-Length", String.valueOf(payloadBytes.length));
        connection.setDoOutput(true);

        try (OutputStream writer = new BufferedOutputStream(connection.getOutputStream())) {
            writer.write(payloadBytes);
        }
        return this;
    }

    public String getResponse() throws IOException {
        try (Scanner scanner = new Scanner(new BufferedInputStream(connection.getInputStream()), CHARSET.name())) {
            return scanner.next();
        }
    }

    public int getCode() throws IOException {
        return connection.getResponseCode();
    }

    public void expectCode(int expected) throws IOException {
        int actual = getCode();

        if (actual != expected)
            throw new IOException(
                    String.format("Got HTTP return code=%d but expected was=%d", actual, expected)
            );
    }
}
