package edu.controller;

import edu.configuration.ApplicationConfig;
import edu.model.web.request.ArticleRequest;
import edu.service.ResponseHandler;
import edu.util.AuthenticationChecker;
import edu.web.ScrapperProducer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        ModelAndView modelAndView = new ModelAndView("Article");
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
}
