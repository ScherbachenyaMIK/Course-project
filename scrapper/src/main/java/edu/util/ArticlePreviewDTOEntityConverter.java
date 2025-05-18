package edu.util;

import edu.model.db.entity.Article;
import edu.model.db.entity.Category;
import edu.model.web.dto.ArticleInformationDTO;
import edu.model.web.dto.ArticlePreviewDTO;
import java.net.URI;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("HideUtilityClassConstructor")
public class ArticlePreviewDTOEntityConverter {
    public static List<ArticlePreviewDTO> convert(List<Article> articles) {
        return articles.stream().map(ArticlePreviewDTOEntityConverter::convert).toList();
    }

    @SuppressWarnings("MagicNumber")
    private static ArticlePreviewDTO convert(Article article) {
        ArticlePreviewDTO.ArticlePreviewDTOBuilder builder = ArticlePreviewDTO.builder();
        ArticleInformationDTO.ArticleInformationDTOBuilder informationBuilder = ArticleInformationDTO.builder();

        builder.articleUri(URI.create("/articles/" + article.getId().toString()));
        builder.title(article.getTitle());
        builder.fragment(
                article.getTextContent()
                        .substring(
                                0,
                                Math.min(article.getTextContent().length(), 500)
                        )
                        .concat("..."));
        builder.previewImageUri(URI.create("/resources/preview/" + article.getId().toString()));

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
}
