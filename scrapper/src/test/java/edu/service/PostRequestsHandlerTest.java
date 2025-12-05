package edu.service;

import edu.model.db.entity.Article;
import edu.model.db.entity.Category;
import edu.model.db.entity.Tag;
import edu.model.db.entity.User;
import edu.model.web.DTO;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.request.ArticleSetupRequest;
import edu.util.ArticleDTOEntityConverter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PostRequestsHandlerTest {
    @Mock
    private ArticlesService articlesService;
    @Mock
    private UsersService usersService;
    @InjectMocks
    private PostRequestsHandler postRequestsHandler;

    private static final User user = User.builder()
            .id(1L)
            .username("username")
            .name("name")
            .sex('M')
            .email("email")
            .userRole("USER")
            .registrationDate(LocalDateTime.now())
            .articles(List.of())
            .build();
    private static final Article article =
            Article.builder()
                    .author(user)
                    .id(1L)
                    .title("title")
                    .textContent("content")
                    .visibility(true)
                    .likes(5)
                    .views(5)
                    .timeToRead(5)
                    .lastUpdateDate(LocalDateTime.now())
                    .creationDate(LocalDateTime.now())
                    .tags(
                            Set.of(
                                    new Tag(
                                            1L,
                                            "tag1",
                                            new HashSet<>()
                                    ),
                                    new Tag(
                                            2L,
                                            "tag2",
                                            new HashSet<>()
                                    )
                            )
                    )
                    .categories(
                            Set.of(
                                    new Category(
                                            1L,
                                            "tag1",
                                            "descr",
                                            new HashSet<>()
                                    ),
                                    new Category(
                                            2L,
                                            "tag2",
                                            "descr",
                                            new HashSet<>()
                                    )
                            )
                    )
                    .build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleArticleSetupRequest() {
        ArticleSetupRequest request =
                new ArticleSetupRequest(
                        1L,
                        "title"
                );
        ArticleDTO response = ArticleDTOEntityConverter.convert(article);

        when(usersService.findUserById(1L))
                .thenReturn(user);
        when(articlesService.setupArticle(
                any()
        ))
                .thenReturn(article);

        DTO result = postRequestsHandler.handleArticleSetupRequest(request);

        assertThat(result).isExactlyInstanceOf(ArticleDTO.class);
        assertThat(result).isEqualTo(response);
    }
}