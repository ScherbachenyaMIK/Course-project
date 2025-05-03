package edu.service;

import edu.configuration.ApplicationConfig;
import edu.model.web.dto.ArticlePreviewDTO;
import edu.model.web.response.CheckAvailabilityResponse;
import edu.util.AuthenticationChecker;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

@Service
@Log4j2
public class ResponseHandler {
    private static final String TIMEOUT_MESSAGE = "Response timeout with id {}";

    @Autowired
    ApplicationConfig applicationConfig;
    private final ConcurrentHashMap<String, ImmutablePair<CompletableFuture<ModelAndView>, String>>
            pendingResponses = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, CompletableFuture<ResponseEntity<?>>>
            pendingApiResponses = new ConcurrentHashMap<>();

    public CompletableFuture<ModelAndView> getResponse(String correlationId, String role) {
        ImmutablePair<CompletableFuture<ModelAndView>, String> entry =
                new ImmutablePair<>(new CompletableFuture<>(), role);
        pendingResponses.put(correlationId, entry);
        entry.getLeft().orTimeout(applicationConfig.timeout(), TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error(TIMEOUT_MESSAGE, correlationId);
                    pendingResponses.remove(correlationId);
                    return null;
                });
        return entry.getLeft();
    }

    public CompletableFuture<ResponseEntity<?>> getApiResponse(String correlationId) {
        CompletableFuture<ResponseEntity<?>> future = new CompletableFuture<>();
        pendingApiResponses.put(correlationId, future);
        future.orTimeout(applicationConfig.timeout(), TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error(TIMEOUT_MESSAGE, correlationId);
                    pendingResponses.remove(correlationId);
                    return null;
                });
        return future;
    }

    public void completeResponseFeed(String correlationId,
                                 List<ArticlePreviewDTO> articles, String modelName) {
        ImmutablePair<CompletableFuture<ModelAndView>, String>
                entry = pendingResponses.remove(correlationId);
        ModelAndView modelAndView = new ModelAndView(modelName);
        if (entry != null) {
            modelAndView.addObject("articles", articles);
            modelAndView.addObject("isAuthenticated",
                    AuthenticationChecker.checkUserAuthentication(entry.getRight())
            );
            entry.getLeft().complete(modelAndView);
        }
    }

    public void completeAvailabilityResponse(String correlationId, CheckAvailabilityResponse response) {
        CompletableFuture<ResponseEntity<?>> future = pendingApiResponses.remove(correlationId);
        if (future != null) {
            future.complete(ResponseEntity.ok(response));
        }
    }
}
