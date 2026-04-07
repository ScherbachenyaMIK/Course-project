package edu.controller;

import edu.configuration.ApplicationConfig;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.request.ArticleEditRequest;
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
    private static final String REDIRECT_ARTICLE_URL = "redirect:/articles/";
    private static final String GET_INFO_TOPIC = "get_info";
    private static final String ARTICLE_ID_KEY = "articleId";
    private static final String CURRENT_USERNAME_KEY = "currentUsername";

    @Autowired
    private ScrapperProducer scrapperProducer;

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private ResponseHandler responseHandler;

    @GetMapping("/{id}")
    public CompletableFuture<ModelAndView> getArticle(@PathVariable Long id) {
        String currentUsername = AuthenticationChecker.getCurrentUsername();
        boolean isAuth = AuthenticationChecker.checkAuthorities();
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendGetRequest(
                GET_INFO_TOPIC,
                correlationId,
                new ArticleRequest(
                        id,
                        applicationConfig.initialCommentsCount(),
                        currentUsername
                ));
        return responseHandler.getResponse(correlationId, isAuth)
                .thenApply(mav -> {
                    if (mav != null) {
                        mav.addObject(ARTICLE_ID_KEY, id);
                        mav.addObject(CURRENT_USERNAME_KEY, currentUsername);
                    }
                    return mav;
                });
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
                        username, title, content, tags, categories
                ));
        return responseHandler.getResponse(correlationId, true);
    }

    @GetMapping("/{id}/edit")
    public CompletableFuture<ModelAndView> getArticleEditForm(@PathVariable Long id) {
        String currentUsername = AuthenticationChecker.getCurrentUsername();
        if (currentUsername == null) {
            CompletableFuture<ModelAndView> future = new CompletableFuture<>();
            future.complete(new ModelAndView(REDIRECT_LOGIN_URL));
            return future;
        }
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendGetRequest(
                GET_INFO_TOPIC,
                correlationId,
                new ArticleRequest(
                        id,
                        0,
                        currentUsername
                ));
        return responseHandler.getResponse(correlationId, true)
                .thenApply(mav -> {
                    if (mav == null) {
                        return null;
                    }
                    Object articleObj = mav.getModel().get("article");
                    if (!(articleObj instanceof ArticleDTO article)
                            || !currentUsername.equals(article.author())) {
                        return new ModelAndView(REDIRECT_ARTICLE_URL + id);
                    }
                    mav.setViewName("ArticleEdit");
                    mav.addObject(ARTICLE_ID_KEY, id);
                    return mav;
                });
    }

    @PostMapping("/{id}/edit")
    public CompletableFuture<ModelAndView> editArticle(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "") String tags,
            @RequestParam(required = false, defaultValue = "") String categories,
            @RequestParam(required = false, defaultValue = "draft") String status,
            @RequestParam(required = false, defaultValue = "30") Integer timeToRead
    ) {
        String username = AuthenticationChecker.getCurrentUsername();
        if (username == null) {
            CompletableFuture<ModelAndView> future = new CompletableFuture<>();
            future.complete(new ModelAndView(REDIRECT_LOGIN_URL));
            return future;
        }
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendPostRequest(
                "articles_editing",
                correlationId,
                new ArticleEditRequest(
                        id, username, title, content, tags, categories,
                        status, timeToRead
                ));
        return responseHandler.getResponse(correlationId, true)
                .thenApply(mav -> new ModelAndView(REDIRECT_ARTICLE_URL + id));
    }
}
