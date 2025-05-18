package edu.util;

import edu.model.db.entity.Article;
import edu.model.db.entity.Category;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.dto.ArticleInformationDTO;
import java.net.URI;
import java.time.ZoneId;
import java.util.stream.Collectors;

@SuppressWarnings("HideUtilityClassConstructor")
public class ArticleDTOEntityConverter {
    public static ArticleDTO convert(Article article) {
        ArticleDTO.ArticleDTOBuilder builder = ArticleDTO.builder();
        ArticleInformationDTO.ArticleInformationDTOBuilder informationBuilder = ArticleInformationDTO.builder();
        builder.title(article.getTitle());
        builder.content(article.getTextContent());
        builder.comments(null);
        builder.lastUpdateDate(article.getLastUpdateDate().atZone(ZoneId.systemDefault()));

        informationBuilder.likes(article.getLikes());
        informationBuilder.views(article.getViews());
        informationBuilder.comments(0);
        informationBuilder.timeToRead(article.getTimeToRead());
        informationBuilder.creationDate(article.getLastUpdateDate().atZone(ZoneId.systemDefault()));
        informationBuilder.status(article.getStatus());
        informationBuilder.tags(article.getTags().stream()
                .map(tag -> "#" + tag.getName())
                .collect(Collectors.joining(" "))
        );
        informationBuilder.categories(article.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.joining(", "))
        );

        builder.information(informationBuilder.build());
        builder.author(article.getAuthor().getUsername());
        builder.authorIconUri(URI.create(
                "/resources/user_icon/" + article.getAuthor().getId().toString()
        ));
        return builder.build();
    }

    public static ArticleDTO emptyDTO() {
        ArticleDTO.ArticleDTOBuilder builder = ArticleDTO.builder();
        return builder.build();
    }
}
