package edu.service;

import edu.model.db.entity.Article;
import edu.model.db.entity.User;
import edu.model.web.DTO;
import edu.model.web.dto.ArticleFeedDTO;
import edu.model.web.request.ArticleRequest;
import edu.model.web.request.ArticlesForFeedRequest;
import edu.model.web.request.ProfileRequest;
import edu.util.ArticleDTOEntityConverter;
import edu.util.ArticlePreviewDTOEntityConverter;
import edu.util.UserDTOEntityConverter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings({"MultipleStringLiterals", "MagicNumber"})
@Service
public class GetRequestsHandler {
    @Autowired
    private ArticlesService articlesService;

    @Autowired
    private UsersService usersService;

    public DTO handleFindArticlesRequest(ArticlesForFeedRequest request) {
        List<Article> articles = articlesService.getArticlesSlice(request.count());
        return new ArticleFeedDTO(ArticlePreviewDTOEntityConverter.convert(articles));
    }

    public DTO handleArticleRequest(ArticleRequest request) {
        Article article = articlesService.getArticle(request.id());
        if (article == null || !article.getVisibility()) {
            return ArticleDTOEntityConverter.emptyDTO();
        }
        return ArticleDTOEntityConverter.convert(article);
    }

    public DTO handleProfileRequest(ProfileRequest request) {
        User user = usersService.findUserByUsername(request.username());
        if (user == null || user.getUserRole().equals("NONE")) {
            return UserDTOEntityConverter.emptyDTO();
        }
        return UserDTOEntityConverter.convert(user);
    }
}
