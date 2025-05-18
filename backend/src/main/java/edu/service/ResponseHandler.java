package edu.service;

import edu.configuration.ApplicationConfig;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.dto.ArticleFeedDTO;
import edu.model.web.dto.UserDTO;
import edu.model.web.response.CheckAvailabilityResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Service
@Log4j2
public class ResponseHandler {
    private static final String TIMEOUT_MESSAGE = "Response timeout with id {}";
    private static final String IS_AUTHENTICATED_ATTRIBUTE_NAME = "isAuthenticated";

    @Autowired
    ApplicationConfig applicationConfig;
    private final ConcurrentHashMap<String, ImmutablePair<CompletableFuture<ModelAndView>, Boolean>>
            pendingResponses = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, CompletableFuture<ResponseEntity<?>>>
            pendingApiResponses = new ConcurrentHashMap<>();

    public CompletableFuture<ModelAndView> getResponse(String correlationId,
                                                       boolean isAuthenticated) {
        ImmutablePair<CompletableFuture<ModelAndView>, Boolean> entry =
                new ImmutablePair<>(new CompletableFuture<>(), isAuthenticated);
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
                                     ArticleFeedDTO articles, String modelName) {
        ImmutablePair<CompletableFuture<ModelAndView>, Boolean>
                entry = pendingResponses.remove(correlationId);
        ModelAndView modelAndView = new ModelAndView(modelName);
        if (entry != null) {
            modelAndView.addObject("articles", articles.articlePreviewDTOList());
            modelAndView.addObject(IS_AUTHENTICATED_ATTRIBUTE_NAME, entry.getRight());
            entry.getLeft().complete(modelAndView);
        }
    }

    public void completeResponseArticle(String correlationId,
                                        ArticleDTO article, String modelName) {
        ImmutablePair<CompletableFuture<ModelAndView>, Boolean>
                entry = pendingResponses.remove(correlationId);
        if (article.title() == null) {
            entry.getLeft().completeExceptionally(
                    new NoResourceFoundException(HttpMethod.GET, "/articles/{id}")
            );
        }
        ModelAndView modelAndView = new ModelAndView(modelName);
        if (entry != null) {
            modelAndView.addObject("article", article);
            modelAndView.addObject(IS_AUTHENTICATED_ATTRIBUTE_NAME, entry.getRight());
            entry.getLeft().complete(modelAndView);
        }
    }

    public void completeResponseProfile(String correlationId,
                                        UserDTO user, String modelName) {
        ImmutablePair<CompletableFuture<ModelAndView>, Boolean>
                entry = pendingResponses.remove(correlationId);
        ModelAndView modelAndView = new ModelAndView(modelName);
        if (entry != null) {
            modelAndView.addObject("user", user);
            modelAndView.addObject(IS_AUTHENTICATED_ATTRIBUTE_NAME, entry.getRight());
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
