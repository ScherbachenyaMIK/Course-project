package edu.web;

import edu.model.web.dto.ImageUploadResponseDTO;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
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

    public Mono<ResponseEntity<ImageUploadResponseDTO>> uploadUserIcon(
            Long userId, byte[] content, String filename, String contentType
    ) {
        return uploadMultipart("/internal/images/user-icon/" + userId, content, filename, contentType);
    }

    public Mono<ResponseEntity<ImageUploadResponseDTO>> uploadPreview(
            Long articleId, byte[] content, String filename, String contentType
    ) {
        return uploadMultipart("/internal/images/preview/" + articleId, content, filename, contentType);
    }

    public Mono<ResponseEntity<ImageUploadResponseDTO>> uploadArticleImage(
            Long articleId, byte[] content, String filename, String contentType
    ) {
        return uploadMultipart(
                "/internal/images/article/" + articleId + "/images",
                content, filename, contentType
        );
    }

    private Mono<ResponseEntity<ImageUploadResponseDTO>> uploadMultipart(
            String uri, byte[] content, String filename, String contentType
    ) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new NamedByteArrayResource(content, filename))
                .filename(filename)
                .contentType(MediaType.parseMediaType(contentType));

        return webClient.post()
                .uri(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new ScrapperUploadException(
                                        response.statusCode().value(), body))))
                .toEntity(ImageUploadResponseDTO.class);
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        private NamedByteArrayResource(byte[] bytes, String filename) {
            super(bytes);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
