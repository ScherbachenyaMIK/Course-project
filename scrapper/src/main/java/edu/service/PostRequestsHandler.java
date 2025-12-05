package edu.service;

import edu.model.db.entity.Article;
import edu.model.web.DTO;
import edu.model.web.request.ArticleSetupRequest;
import edu.util.ArticleDTOEntityConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostRequestsHandler {
    @Autowired
    private ArticlesService articlesService;

    @Autowired
    private UsersService usersService;

    public DTO handleArticleSetupRequest(ArticleSetupRequest request) {
        Article.ArticleBuilder articleBuilder = Article.builder();
        articleBuilder.title(request.title());
        articleBuilder.author(usersService.findUserById(request.authorId()));
        articleBuilder.textContent("");

        Article article = articlesService.setupArticle(articleBuilder.build());

        return ArticleDTOEntityConverter.convert(article);
    }
}
