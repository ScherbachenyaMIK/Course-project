package edu.controller;

import edu.configuration.ApplicationConfig;
import edu.model.web.request.ArticleRequest;
import edu.model.web.request.ArticleSetupRequest;
import edu.service.ResponseHandler;
import edu.util.AuthenticationChecker;
import edu.web.ScrapperProducer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/articles")
public class ArticlesController {
    @Autowired
    private ScrapperProducer scrapperProducer;

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private ResponseHandler responseHandler;

    @GetMapping("/{id}")
    public CompletableFuture<ModelAndView> getArticle(@PathVariable Long id) {
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendGetRequest(
                "get_info",
                correlationId,
                new ArticleRequest(
                        id,
                        applicationConfig.initialCommentsCount()
                ));
        return responseHandler.getResponse(correlationId, AuthenticationChecker.checkAuthorities());
    }

    @PostMapping("/{authorId}")
    public CompletableFuture<ModelAndView> postArticle(@PathVariable Long authorId) {
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendPostRequest(
                "articles_setup",
                correlationId,
                new ArticleSetupRequest(
                        authorId,
                        "Title"
                ));
        return responseHandler.getResponse(correlationId, AuthenticationChecker.checkAuthorities());
    }
}
