package edu.service;

import edu.model.db.entity.Article;
import edu.model.db.entity.Category;
import edu.model.db.entity.Comment;
import edu.model.db.entity.Tag;
import edu.model.db.entity.User;
import edu.model.web.DTO;
import edu.model.web.dto.CommentDTO;
import edu.model.web.request.ArticleEditRequest;
import edu.model.web.request.ArticleSetupRequest;
import edu.model.web.request.CommentRequest;
import edu.model.web.request.EditProfileRequest;
import edu.model.web.request.LikeRequest;
import edu.model.web.request.ViewRequest;
import edu.util.ArticleDTOEntityConverter;
import edu.util.UserDTOEntityConverter;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class PostRequestsHandler {
    @Autowired
    private ArticlesService articlesService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private TagsService tagsService;

    @Autowired
    private CategoriesService categoriesService;

    @Autowired
    private CommentsService commentsService;

    public DTO handleArticleSetupRequest(ArticleSetupRequest request) {
        Set<Tag> tagSet = parseTags(request.tags());
        Set<Category> categorySet = parseCategories(request.categories());

        Article article = Article.builder()
                .title(request.title())
                .author(usersService.findUserByUsername(request.username()))
                .textContent(request.content() != null ? request.content() : "")
                .tags(tagSet)
                .categories(categorySet)
                .build();

        Article saved = articlesService.setupArticle(article);
        return ArticleDTOEntityConverter.convert(saved, Collections.emptyList());
    }

    @SuppressWarnings("ReturnCount")
    public DTO handleArticleEditRequest(ArticleEditRequest request) {
        Article article = articlesService.getArticle(request.articleId());
        if (article == null) {
            return ArticleDTOEntityConverter.emptyDTO();
        }
        if (!article.getAuthor().getUsername().equals(request.username())) {
            return ArticleDTOEntityConverter.emptyDTO();
        }

        article.setTitle(request.title());
        article.setTextContent(request.content() != null ? request.content() : "");
        article.setTags(parseTags(request.tags()));
        article.setCategories(parseCategories(request.categories()));
        if (request.status() != null && !request.status().isBlank()) {
            article.setStatus(request.status());
            article.setVisibility("published".equals(request.status()));
        }
        if (request.timeToRead() != null) {
            article.setTimeToRead(request.timeToRead());
        }
        article.setLastUpdateDate(LocalDateTime.now());

        Article saved = articlesService.setupArticle(article);
        return ArticleDTOEntityConverter.convert(saved, Collections.emptyList());
    }

    public DTO handleEditProfileRequest(EditProfileRequest request) {
        User user = usersService.updateProfile(request);
        if (user == null) {
            return UserDTOEntityConverter.emptyDTO();
        }
        return UserDTOEntityConverter.convert(user);
    }

    public void handleViewRequest(ViewRequest request) {
        articlesService.incrementViews(request.articleId());
    }

    public void handleLikeRequest(LikeRequest request) {
        articlesService.incrementLikes(request.articleId());
    }

    @SuppressWarnings("ReturnCount")
    public DTO handleCommentRequest(CommentRequest request) {
        Article article = articlesService.getArticle(request.articleId());
        if (article == null) {
            return ArticleDTOEntityConverter.emptyDTO();
        }
        User user = usersService.findUserByUsername(request.username());
        if (user == null) {
            return ArticleDTOEntityConverter.emptyDTO();
        }

        Comment comment = Comment.builder()
                .article(article)
                .user(user)
                .commentText(request.text())
                .build();
        Comment saved = commentsService.save(comment);

        return CommentDTO.builder()
                .author(saved.getUser().getUsername())
                .authorIconUri(URI.create(
                        "/resources/user_icon/"
                                + saved.getUser().getId().toString()))
                .text(saved.getCommentText())
                .date(saved.getCommentDate().atZone(ZoneId.systemDefault()))
                .build();
    }

    private Set<Tag> parseTags(String tags) {
        Set<Tag> tagSet = new HashSet<>();
        if (tags == null || tags.isBlank()) {
            return tagSet;
        }
        for (String tagName : tags.split(",")) {
            String trimmed = tagName.trim();
            if (!trimmed.isEmpty()) {
                tagSet.add(tagsService.findOrCreate(trimmed));
            }
        }
        return tagSet;
    }

    private Set<Category> parseCategories(String categories) {
        Set<Category> categorySet = new HashSet<>();
        if (categories == null || categories.isBlank()) {
            return categorySet;
        }
        for (String catName : categories.split(",")) {
            String trimmed = catName.trim();
            if (!trimmed.isEmpty()) {
                Category category = categoriesService.findByName(trimmed);
                if (category != null) {
                    categorySet.add(category);
                }
            }
        }
        return categorySet;
    }
}
