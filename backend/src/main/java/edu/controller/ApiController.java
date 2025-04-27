package edu.controller;

import edu.model.web.request.CheckAvailabilityRequest;
import edu.model.web.response.CheckAvailabilityResponse;
import edu.service.ResponseHandler;
import edu.web.ScrapperProducer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
}
