package edu.web;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class ScrapperClientTest {
    private static final int PORT = 8080;
    private static final String HOST = "localhost";

    private ScrapperClient scrapperClient = new ScrapperClient(
            "http://localhost:8080",
            10485760,
            10
    );
    private WireMockServer wireMockServer;

    @BeforeEach
    public void setUp() {
        wireMockServer = new WireMockServer(PORT);
        wireMockServer.start();
        WireMock.configureFor(HOST, PORT);
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void getImage() {
        byte[] imageBytes = new byte[]{1, 2, 3, 4};

        wireMockServer.stubFor(get(urlPathMatching("/images/1"))
                .withQueryParam("query", equalTo("/article/1/images/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "image/png")
                        .withHeader("Cache-Control", "max-age=604800, public")
                        .withBody(imageBytes)));

        Mono<ResponseEntity<byte[]>> response = scrapperClient
                .getImage(1L, "/article/1/images/");

        StepVerifier.create(response)
                .expectNextMatches(entity ->
                        entity.getStatusCode().is2xxSuccessful()
                                && Objects.equals(entity.getHeaders().getContentType(), MediaType.IMAGE_PNG)
                                && entity.getBody() != null
                                && Objects.equals(entity.getBody().length, 4)
                )
                .verifyComplete();
    }

    @Test
    void getImageNotFound() {
        wireMockServer.stubFor(get(urlPathMatching("/images/1"))
                .withQueryParam("query", equalTo("/article/1/images/"))
                .willReturn(aResponse().withStatus(404)));

        Mono<ResponseEntity<byte[]>> response = scrapperClient
                .getImage(1L, "/article/1/images/");

        StepVerifier.create(response)
                .expectErrorMatches(error ->
                        error instanceof WebClientResponseException.NotFound &&
                                ((WebClientResponseException) error).getStatusCode().value() == 404
                )
                .verify();
    }
}