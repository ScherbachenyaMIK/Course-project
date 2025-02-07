package edu.service;

import edu.configuration.ApplicationConfig;
import edu.model.web.dto.ArticlePreviewDTO;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

@Service
@Log4j2
public class ResponseHandler {
    @Autowired
    ApplicationConfig applicationConfig;
    private final ConcurrentHashMap<String, CompletableFuture<ModelAndView>> pendingResponses =
            new ConcurrentHashMap<>();

    public CompletableFuture<ModelAndView> getResponse(String correlationId) {
        CompletableFuture<ModelAndView> future = new CompletableFuture<>();
        pendingResponses.put(correlationId, future);
        future.orTimeout(applicationConfig.timeout(), TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("Response timeout with id {}", correlationId);
                    pendingResponses.remove(correlationId);
                    return null;
                });
        return future;
    }

    public void completeResponseFeed(String correlationId,
                                 List<ArticlePreviewDTO> articles, String modelName) {
        CompletableFuture<ModelAndView> future = pendingResponses.remove(correlationId);
        ModelAndView modelAndView = new ModelAndView(modelName);
        if (future != null) {
            modelAndView.addObject("articles", articles);
            future.complete(modelAndView);
        }
    }
}
