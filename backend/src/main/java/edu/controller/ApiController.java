package edu.controller;

import edu.model.web.dto.AIResponseDTO;
import edu.model.web.request.AIRequest;
import edu.model.web.request.CheckAvailabilityRequest;
import edu.model.web.response.CheckAvailabilityResponse;
import edu.service.ResponseHandler;
import edu.web.ScrapperProducer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private ScrapperProducer scrapperProducer;

    @Autowired
    private ResponseHandler responseHandler;

    @SuppressWarnings("unchecked")
    @PostMapping("/availability")
    public CompletableFuture<ResponseEntity<CheckAvailabilityResponse>> checkEmailAndUsername(
            @RequestBody CheckAvailabilityRequest request) {
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendAuthRequest(
                correlationId,
                request
        );
        return (CompletableFuture<ResponseEntity<CheckAvailabilityResponse>>)
                (CompletableFuture<?>) responseHandler.getApiResponse(correlationId);
    }

    @GetMapping("/get/username")
    public ResponseEntity<String> getUsername() {
        return ResponseEntity.ok(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/ai/sample")
    public CompletableFuture<ResponseEntity<AIResponseDTO>> getSampleAI() {
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendGetRequest(
                "get_info",
                correlationId,
                new AIRequest("", "Sample")
        );
        return (CompletableFuture<ResponseEntity<AIResponseDTO>>)
                (CompletableFuture<?>) responseHandler.getApiResponse(correlationId);
    }
}
