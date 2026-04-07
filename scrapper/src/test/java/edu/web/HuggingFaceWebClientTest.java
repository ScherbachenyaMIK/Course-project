package edu.web;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class HuggingFaceWebClientTest {

    @Test
    void constructorInitializesWithoutErrors() {
        WebClient.Builder builder = WebClient.builder();
        HuggingFaceWebClient client = new HuggingFaceWebClient(builder, "test-token");

        assertThat(client).isNotNull();
    }
}
