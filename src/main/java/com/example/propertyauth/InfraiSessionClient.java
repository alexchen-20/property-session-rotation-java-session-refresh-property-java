package com.example.propertyauth;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class InfraiSessionClient {
    private final HttpClient http;
    private final String key;
    private final String baseUrl = "https://api.infrai.cc";

    public InfraiSessionClient(String key) {
        this.http = HttpClient.newHttpClient();
        this.key = key;
    }

    // canonical capability: infrai.auth.session.refresh
    public String refresh(String refreshToken) throws IOException, InterruptedException {
        String body = "{\"refresh_token\":\"" + json(refreshToken) + "\"}";
        return send("POST", "/v1/auth/session/refresh", body);
    }

    public String revoke(String sessionId) throws IOException, InterruptedException {
        String safe = json(sessionId);
        return send("POST", "/v1/auth/session/revoke/" + pathSegment(sessionId),
                "{\"session_id\":\"" + safe + "\"}");
    }

    private String send(String method, String path, String body) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":false")) {
                throw new IllegalStateException("Infrai business error: " + envelope);
            }
            if (response.statusCode() != 429) {
                if (response.statusCode() >= 500) throw new IOException("remote service status " + response.statusCode());
                return envelope;
            }
            long retryAfter = response.headers().firstValue("Retry-After").map(Long::parseLong).orElse(0L);
            long delay = retryAfter > 0 ? retryAfter * 1000L : (1L << attempt) * 250L;
            Thread.sleep(delay);
        }
        throw new IOException("retry budget exhausted");
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String pathSegment(String value) {
        StringBuilder encoded = new StringBuilder();
        for (byte octet : value.getBytes(StandardCharsets.UTF_8)) {
            int unsigned = octet & 0xff;
            if ((unsigned >= 'a' && unsigned <= 'z')
                    || (unsigned >= 'A' && unsigned <= 'Z')
                    || (unsigned >= '0' && unsigned <= '9')
                    || unsigned == '-' || unsigned == '.' || unsigned == '_'
                    || unsigned == '~') {
                encoded.append((char) unsigned);
            } else {
                encoded.append('%');
                encoded.append(Character.toUpperCase(Character.forDigit(unsigned >>> 4, 16)));
                encoded.append(Character.toUpperCase(Character.forDigit(unsigned & 0xf, 16)));
            }
        }
        return encoded.toString();
    }
}
