package edu.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class StatusCodeDescriptor {
    private final Properties properties = new Properties();

    StatusCodeDescriptor() throws IOException {
        try (Reader reader = new InputStreamReader(
                new ClassPathResource("HttpResponsesDescription.yml").getInputStream(),
                StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
    }

    public String getDescription(int code) {
        return properties.getProperty(String.valueOf(code));
    }

}
