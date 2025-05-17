package edu.web;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Component
public class ScrapperClient {
    private final WebClient webClient;

    public ScrapperClient(
            @Value("${scrapper.base-url}") String baseUrl,
            @Value("${scrapper.cache-size}") int cacheSize,
            @Value("${app.timeout}") int timeout
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(timeout))
                                .wiretap(true)
                ))
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(cacheSize)
                )
                .build();
    }

    public Mono<ResponseEntity<byte[]>> getImage(Long id, String queryParam) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/images/{id}")
                        .queryParam("query", queryParam)
                        .build(id))
                .accept(MediaType.ALL)
                .retrieve()
                .toEntity(byte[].class)
                .cache(Duration.of(1, ChronoUnit.DAYS));
    }
}
