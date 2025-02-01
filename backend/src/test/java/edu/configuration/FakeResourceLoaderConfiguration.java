package edu.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@TestConfiguration
public class FakeResourceLoaderConfiguration {
    @Bean
    @Primary
    public ResourceLoader resourceLoader() {
        return new ResourceLoader() {
            @Override
            public Resource getResource(String location) {
                return null;
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }
        };
    }
}
