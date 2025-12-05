package edu.service;

import edu.model.db.entity.Article;
import edu.model.db.entity.Category;
import edu.model.db.entity.Tag;
import edu.model.db.entity.User;
import edu.model.web.DTO;
import edu.model.web.dto.AIResponseDTO;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.dto.ArticleFeedDTO;
import edu.model.web.dto.UserDTO;
import edu.model.web.request.AIRequest;
import edu.model.web.request.ArticleRequest;
import edu.model.web.request.ArticlesForFeedRequest;
import edu.model.web.request.ProfileRequest;
import edu.util.ArticleDTOEntityConverter;
import edu.util.ArticlePreviewDTOEntityConverter;
import edu.util.UserDTOEntityConverter;
import edu.web.HuggingFaceWebClient;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class GetRequestsHandlerTest {
    @Mock
    private ArticlesService articlesService;
    @Mock
    private UsersService usersService;
    @Mock
    private HuggingFaceWebClient webClient;
    @InjectMocks
    private GetRequestsHandler getRequestsHandler;

    private static User user = User.builder()
            .id(1L)
            .username("username")
            .name("name")
            .sex('M')
            .email("email")
            .userRole("USER")
            .registrationDate(LocalDateTime.now())
            .articles(List.of())
            .build();
    private static List<Article> articleList = List.of(
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
                    .build(),
            Article.builder()
                    .author(user)
                    .id(2L)
                    .title("title2")
                    .textContent("content2")
                    .visibility(false)
                    .likes(5)
                    .views(5)
                    .timeToRead(5)
                    .lastUpdateDate(LocalDateTime.now())
                    .creationDate(LocalDateTime.now())
                    .tags(new HashSet<>())
                    .categories(new HashSet<>())
                    .build(),
            Article.builder()
                    .author(user)
                    .id(3L)
                    .title("title3")
                    .textContent("content3")
                    .visibility(true)
                    .likes(5)
                    .views(5)
                    .timeToRead(5)
                    .lastUpdateDate(LocalDateTime.now())
                    .creationDate(LocalDateTime.now())
                    .tags(new HashSet<>())
                    .categories(new HashSet<>())
                    .build()
    );

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleFindArticlesRequest() {
        ArticlesForFeedRequest request =
                new ArticlesForFeedRequest(
                        5
                );
        ArticleFeedDTO response =
                new ArticleFeedDTO(
                        ArticlePreviewDTOEntityConverter.convert(articleList)
                );

        when(articlesService.getArticlesSlice(5))
                .thenReturn(articleList);

        DTO result = getRequestsHandler.handleFindArticlesRequest(request);

        assertThat(result).isExactlyInstanceOf(ArticleFeedDTO.class);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleArticleRequest() {
        ArticleRequest request =
                new ArticleRequest(
                        1L,
                        5
                );
        ArticleDTO response = ArticleDTOEntityConverter.convert(articleList.getFirst());

        when(articlesService.getArticle(1L))
                .thenReturn(articleList.getFirst());

        DTO result = getRequestsHandler.handleArticleRequest(request);

        assertThat(result).isExactlyInstanceOf(ArticleDTO.class);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleProfileRequest() {
        ProfileRequest request =
                new ProfileRequest(
                        "username"
                );
        UserDTO response = UserDTOEntityConverter.convert(user);

        when(usersService.findUserByUsername("username"))
                .thenReturn(user);

        DTO result = getRequestsHandler.handleProfileRequest(request);

        assertThat(result).isExactlyInstanceOf(UserDTO.class);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleProfileRequestNoSuchUser() {
        ProfileRequest request =
                new ProfileRequest(
                        "username"
                );
        UserDTO response = UserDTOEntityConverter.emptyDTO();

        when(usersService.findUserByUsername("username"))
                .thenReturn(null);

        DTO result = getRequestsHandler.handleProfileRequest(request);

        assertThat(result).isExactlyInstanceOf(UserDTO.class);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleProfileRequestWithoutRole() {
        ProfileRequest request =
                new ProfileRequest(
                        "username"
                );
        UserDTO response = UserDTOEntityConverter.emptyDTO();

        when(usersService.findUserByUsername("username"))
                .thenReturn(User.builder().userRole("NONE").build());

        DTO result = getRequestsHandler.handleProfileRequest(request);

        assertThat(result).isExactlyInstanceOf(UserDTO.class);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleAISampleRequest() {
        AIRequest request =
                new AIRequest(
                        "",
                        "Sample"
                );
        AIResponseDTO response = new AIResponseDTO("response");

        when(webClient.sample())
                .thenReturn("response");

        DTO result = getRequestsHandler.handleAIRequest(request);

        assertThat(result).isExactlyInstanceOf(AIResponseDTO.class);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleAIUnknownRequest() {
        AIRequest request =
                new AIRequest(
                        "",
                        "wrongType"
                );

        assertThatThrownBy(
                () -> getRequestsHandler.handleAIRequest(request)
        ).isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown request type: wrongType");
    }
}