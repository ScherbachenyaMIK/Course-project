package edu.web;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HuggingFaceWebClient {
    private static final String INPUTS_ATTRIBUTE_NAME = "inputs";
    private static final String PARAMETERS_ATTRIBUTE_NAME = "parameters";
    private static final String MAX_NEW_TOKENS_ATTRIBUTE_NAME = "max_new_tokens";
    private static final String HELSINKI_NLP_RESPONSE_FIELD = "translation_text";
    private static final String DEEP_SEEK_CONTENT_FIELD = "content";

    private final WebClient webClientRuEn;

    private final WebClient webClientEnRu;

    private final WebClient webClient;

    private final String apiToken;

    public HuggingFaceWebClient(
            WebClient.Builder huggingFaceWebClientBuilder,
            @Value("${huggingface.api.token}") String apiToken
    ) {
        this.webClientRuEn = huggingFaceWebClientBuilder
                .baseUrl("https://router.huggingface.co/hf-inference/models/Helsinki-NLP/opus-mt-ru-en")
                .build();
        this.webClientEnRu = huggingFaceWebClientBuilder
                .baseUrl("https://router.huggingface.co/hf-inference/models/Helsinki-NLP/opus-mt-en-ru")
                .build();
        this.webClient = huggingFaceWebClientBuilder
                .baseUrl("https://router.huggingface.co/novita/v3/openai/chat/completions")
                .build();
        this.apiToken = apiToken;
    }

    @SuppressWarnings("MagicNumber")
    public String sample() {
        String authorizationHeaderName = "Authorization";
        String authorizationHeaderValue = "Bearer " + apiToken;

        Map<String, Object> requestTranslateBody = Map.of(
                INPUTS_ATTRIBUTE_NAME, "Опиши осеннюю дождливую погоду красиво и поэтично.",
                PARAMETERS_ATTRIBUTE_NAME, Map.of(MAX_NEW_TOKENS_ATTRIBUTE_NAME, 50)
        );

        String enText = webClientRuEn.post()
                .header(authorizationHeaderName, authorizationHeaderValue)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestTranslateBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .map(responseList -> {
                    Map<String, Object> firstItem = responseList.get(0);
                    return (String) firstItem.get(HELSINKI_NLP_RESPONSE_FIELD);
                })
                .block();

        Map<String, Object> requestGenerateBody = Map.of(
                "model", "deepseek/deepseek-r1-turbo",
                "messages", new Object[]{
                        Map.of(
                                "role", "user",
                                DEEP_SEEK_CONTENT_FIELD, Objects.requireNonNull(enText))
                },
                "max_tokens", 100
        );

        String content = webClient.post()
                .header(authorizationHeaderName, authorizationHeaderValue)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestGenerateBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(root -> root
                        .get("choices")
                        .get(0)
                        .get("message")
                        .get(DEEP_SEEK_CONTENT_FIELD)
                        .asText()
                )
                .block();

        String cleanedContent = Objects.requireNonNull(content).replaceAll(
                "(?s)<think>.*?</think>", ""
        ).trim();

        Map<String, Object> requestInvTranslateBody = Map.of(
                INPUTS_ATTRIBUTE_NAME, cleanedContent,
                PARAMETERS_ATTRIBUTE_NAME, Map.of(MAX_NEW_TOKENS_ATTRIBUTE_NAME, 50)
        );

        return webClientEnRu.post()
                .header(authorizationHeaderName, authorizationHeaderValue)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestInvTranslateBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .map(responseList -> {
                    Map<String, Object> firstItem = responseList.get(0);
                    return (String) firstItem.get(HELSINKI_NLP_RESPONSE_FIELD);
                })
                .block()
                .concat(" ...");
    }
}
