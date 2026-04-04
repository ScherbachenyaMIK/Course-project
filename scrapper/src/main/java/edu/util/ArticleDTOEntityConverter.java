package edu.util;

import edu.model.db.entity.Article;
import edu.model.db.entity.Category;
import edu.model.db.entity.Comment;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.dto.ArticleInformationDTO;
import edu.model.web.dto.CommentDTO;
import java.net.URI;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"HideUtilityClassConstructor", "MultipleStringLiterals"})
public class ArticleDTOEntityConverter {
    public static ArticleDTO convert(Article article, List<Comment> comments) {
        ArticleDTO.ArticleDTOBuilder builder = ArticleDTO.builder();
        ArticleInformationDTO.ArticleInformationDTOBuilder informationBuilder = ArticleInformationDTO.builder();
        builder.title(article.getTitle());
        builder.content(article.getTextContent());
        builder.lastUpdateDate(article.getLastUpdateDate().atZone(ZoneId.systemDefault()));

        ArrayList<CommentDTO> commentDTOs = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                commentDTOs.add(CommentDTO.builder()
                        .author(comment.getUser().getUsername())
                        .authorIconUri(URI.create(
                                "/resources/user_icon/"
                                        + comment.getUser().getId().toString()))
                        .text(comment.getCommentText())
                        .date(comment.getCommentDate()
                                .atZone(ZoneId.systemDefault()))
                        .build());
            }
        }
        builder.comments(commentDTOs);

        informationBuilder.likes(article.getLikes());
        informationBuilder.views(article.getViews());
        informationBuilder.comments(commentDTOs.size());
        informationBuilder.timeToRead(article.getTimeToRead());
        informationBuilder.creationDate(article.getLastUpdateDate().atZone(ZoneId.systemDefault()));
        informationBuilder.status(article.getStatus());
        informationBuilder.tags(article.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.joining(", "))
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
