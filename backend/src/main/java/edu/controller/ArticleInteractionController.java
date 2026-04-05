package edu.controller;

import edu.model.web.request.CommentRequest;
import edu.model.web.request.LikeRequest;
import edu.model.web.request.ViewRequest;
import edu.service.ResponseHandler;
import edu.util.AuthenticationChecker;
import edu.web.ScrapperProducer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
public class ArticleInteractionController {
    @Autowired
    private ScrapperProducer scrapperProducer;

    @Autowired
    private ResponseHandler responseHandler;

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> recordView(@PathVariable Long id) {
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendPostRequest(
                "article_views", correlationId, new ViewRequest(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeArticle(@PathVariable Long id) {
        String username = AuthenticationChecker.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendPostRequest(
                "article_likes", correlationId,
                new LikeRequest(id, username));
        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/{id}/comments")
    public CompletableFuture<ResponseEntity<?>> addComment(
            @PathVariable Long id,
            @RequestParam String text
    ) {
        String username = AuthenticationChecker.getCurrentUsername();
        if (username == null) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendPostRequest(
                "commenting", correlationId,
                new CommentRequest(id, username, text));
        return (CompletableFuture<ResponseEntity<?>>)
                (CompletableFuture<?>) responseHandler
                        .getApiResponse(correlationId);
    }
}
