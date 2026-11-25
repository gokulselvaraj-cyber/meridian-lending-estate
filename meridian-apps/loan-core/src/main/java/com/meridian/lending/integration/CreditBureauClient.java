package com.meridian.lending.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Fetches a credit score from the external credit bureau (outside Meridian). */
@Component
public class CreditBureauClient {
    private final URI url;
    private final Duration timeout;
    private final int retries;
    private final HttpClient http = HttpClient.newHttpClient();

    public CreditBureauClient(@Value("${lending.bureau.url}") String url,
                              @Value("${lending.bureau.timeout-seconds}") int timeoutSeconds,
                              @Value("${lending.bureau.retries}") int retries) {
        this.url = URI.create(url);
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.retries = retries;
    }

    public int score(String customerId) {
        RuntimeException last = null;
        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                HttpRequest req = HttpRequest.newBuilder(url.resolve("?customer=" + customerId))
                        .timeout(timeout).GET().build();
                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
                return Integer.parseInt(res.body().trim());
            } catch (Exception e) {
                last = new IllegalStateException("credit bureau unavailable", e);
            }
        }
        throw last;
    }
}
