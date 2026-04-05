package edu.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resend")
public record ResendProperties(
        String apiKey,
        String from,
        Confirmation confirmation
) {
    public record Confirmation(
            Duration expiration,
            String baseUrl
    ) {
    }
}
