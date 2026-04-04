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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/articles")
public class ArticlesController {
    private static final String REDIRECT_LOGIN_URL = "redirect:/login";

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
                        applicationConfig.initialCommentsCount(),
                        AuthenticationChecker.getCurrentUsername()
                ));
        return responseHandler.getResponse(correlationId, AuthenticationChecker.checkAuthorities());
    }

    @GetMapping("/new")
    public ModelAndView getArticleCreateForm() {
        if (!AuthenticationChecker.checkAuthorities()) {
            return new ModelAndView(REDIRECT_LOGIN_URL);
        }
        return new ModelAndView("ArticleCreate");
    }

    @PostMapping("/new")
    public CompletableFuture<ModelAndView> createArticle(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "") String tags,
            @RequestParam(required = false, defaultValue = "") String categories
    ) {
        String username = AuthenticationChecker.getCurrentUsername();
        if (username == null) {
            CompletableFuture<ModelAndView> future = new CompletableFuture<>();
            future.complete(new ModelAndView(REDIRECT_LOGIN_URL));
            return future;
        }
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendPostRequest(
                "articles_setup",
                correlationId,
                new ArticleSetupRequest(
                        username,
                        title,
                        content,
                        tags,
                        categories
                ));
        return responseHandler.getResponse(correlationId, true);
    }
}
