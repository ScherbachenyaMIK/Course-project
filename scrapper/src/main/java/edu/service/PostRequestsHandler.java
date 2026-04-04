package edu.service;

import edu.model.db.entity.Article;
import edu.model.db.entity.Category;
import edu.model.db.entity.Tag;
import edu.model.web.DTO;
import edu.model.web.request.ArticleSetupRequest;
import edu.util.ArticleDTOEntityConverter;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostRequestsHandler {
    @Autowired
    private ArticlesService articlesService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private TagsService tagsService;

    @Autowired
    private CategoriesService categoriesService;

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
        return ArticleDTOEntityConverter.convert(saved);
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
