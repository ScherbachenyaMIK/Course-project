package edu.service;

import edu.model.db.entity.Article;
import edu.model.db.entity.Category;
import edu.model.db.entity.Comment;
import edu.model.db.entity.Tag;
import edu.model.db.entity.User;
import edu.model.web.DTO;
import edu.model.web.dto.ArticleDTO;
import edu.model.web.dto.CommentDTO;
import edu.model.web.request.ArticleEditRequest;
import edu.model.web.request.ArticleSetupRequest;
import edu.model.web.request.CommentRequest;
import edu.model.web.request.LikeRequest;
import edu.model.web.request.ViewRequest;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostRequestsHandlerTest {
    @Mock
    private ArticlesService articlesService;
    @Mock
    private UsersService usersService;
    @Mock
    private TagsService tagsService;
    @Mock
    private CategoriesService categoriesService;
    @Mock
    private CommentsService commentsService;
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
                                            "cat1",
                                            "descr",
                                            new HashSet<>()
                                    ),
                                    new Category(
                                            2L,
                                            "cat2",
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
                        "username",
                        "title",
                        "content",
                        "tag1, tag2",
                        "cat1, cat2"
                );
        ArticleDTO response = ArticleDTOEntityConverter.convert(article, java.util.Collections.emptyList());

        when(usersService.findUserByUsername("username"))
                .thenReturn(user);
        when(tagsService.findOrCreate("tag1"))
                .thenReturn(new Tag(1L, "tag1", new HashSet<>()));
        when(tagsService.findOrCreate("tag2"))
                .thenReturn(new Tag(2L, "tag2", new HashSet<>()));
        when(categoriesService.findByName("cat1"))
                .thenReturn(new Category(1L, "cat1", "descr", new HashSet<>()));
        when(categoriesService.findByName("cat2"))
                .thenReturn(new Category(2L, "cat2", "descr", new HashSet<>()));
        when(articlesService.setupArticle(any()))
                .thenReturn(article);

        DTO result = postRequestsHandler.handleArticleSetupRequest(request);

        assertThat(result).isExactlyInstanceOf(ArticleDTO.class);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void handleArticleEditRequest() {
        ArticleEditRequest request =
                new ArticleEditRequest(
                        1L, "username", "new title",
                        "new content", "tag1", "cat1",
                        "published", 15
                );

        when(articlesService.getArticle(1L))
                .thenReturn(article);
        when(tagsService.findOrCreate("tag1"))
                .thenReturn(new Tag(1L, "tag1", new HashSet<>()));
        when(categoriesService.findByName("cat1"))
                .thenReturn(new Category(1L, "cat1", "descr", new HashSet<>()));
        when(articlesService.setupArticle(any()))
                .thenReturn(article);

        DTO result = postRequestsHandler.handleArticleEditRequest(request);

        assertThat(result).isExactlyInstanceOf(ArticleDTO.class);
        assertThat(article.getVisibility()).isTrue();
        assertThat(article.getStatus()).isEqualTo("published");
        assertThat(article.getTimeToRead()).isEqualTo(15);
    }

    @Test
    void handleArticleEditRequestDraftSetsVisibilityFalse() {
        ArticleEditRequest request =
                new ArticleEditRequest(
                        1L, "username", "title",
                        "content", "", "", "draft", 30
                );

        when(articlesService.getArticle(1L))
                .thenReturn(article);
        when(articlesService.setupArticle(any()))
                .thenReturn(article);

        postRequestsHandler.handleArticleEditRequest(request);

        assertThat(article.getVisibility()).isFalse();
        assertThat(article.getStatus()).isEqualTo("draft");
    }

    @Test
    void handleArticleEditRequestWrongUser() {
        ArticleEditRequest request =
                new ArticleEditRequest(
                        1L, "wronguser", "title",
                        "content", "", "", "draft", 30
                );

        when(articlesService.getArticle(1L))
                .thenReturn(article);

        DTO result = postRequestsHandler.handleArticleEditRequest(request);

        assertThat(result).isEqualTo(ArticleDTOEntityConverter.emptyDTO());
    }

    @Test
    void handleArticleEditRequestArticleNotFound() {
        ArticleEditRequest request =
                new ArticleEditRequest(
                        99L, "username", "title",
                        "content", "", "", "draft", 30
                );

        when(articlesService.getArticle(99L))
                .thenReturn(null);

        DTO result = postRequestsHandler.handleArticleEditRequest(request);

        assertThat(result).isEqualTo(ArticleDTOEntityConverter.emptyDTO());
    }

    @Test
    void handleArticleSetupRequestWithEmptyTagsAndCategories() {
        ArticleSetupRequest request =
                new ArticleSetupRequest(
                        "username",
                        "title",
                        "content",
                        "",
                        ""
                );

        Article articleNoTags = Article.builder()
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
                .tags(new HashSet<>())
                .categories(new HashSet<>())
                .build();

        when(usersService.findUserByUsername("username"))
                .thenReturn(user);
        when(articlesService.setupArticle(any()))
                .thenReturn(articleNoTags);

        DTO result = postRequestsHandler.handleArticleSetupRequest(request);

        assertThat(result).isExactlyInstanceOf(ArticleDTO.class);
    }

    @Test
    void handleViewRequest() {
        ViewRequest request = new ViewRequest(1L);
        postRequestsHandler.handleViewRequest(request);
        verify(articlesService).incrementViews(1L);
    }

    @Test
    void handleLikeRequest() {
        LikeRequest request = new LikeRequest(1L, "username");
        postRequestsHandler.handleLikeRequest(request);
        verify(articlesService).incrementLikes(1L);
    }

    @Test
    void handleCommentRequest() {
        CommentRequest request = new CommentRequest(1L, "username", "Great article!");
        Comment saved = Comment.builder()
                .id(1L)
                .user(user)
                .article(article)
                .commentText("Great article!")
                .commentDate(LocalDateTime.now())
                .build();

        when(articlesService.getArticle(1L)).thenReturn(article);
        when(usersService.findUserByUsername("username")).thenReturn(user);
        when(commentsService.save(any())).thenReturn(saved);

        DTO result = postRequestsHandler.handleCommentRequest(request);

        assertThat(result).isExactlyInstanceOf(CommentDTO.class);
        CommentDTO commentDTO = (CommentDTO) result;
        assertThat(commentDTO.author()).isEqualTo("username");
        assertThat(commentDTO.text()).isEqualTo("Great article!");
    }

    @Test
    void handleCommentRequestArticleNotFound() {
        CommentRequest request = new CommentRequest(99L, "username", "text");

        when(articlesService.getArticle(99L)).thenReturn(null);

        DTO result = postRequestsHandler.handleCommentRequest(request);
        assertThat(result).isEqualTo(ArticleDTOEntityConverter.emptyDTO());
    }

    @Test
    void handleCommentRequestUserNotFound() {
        CommentRequest request = new CommentRequest(1L, "unknown", "text");

        when(articlesService.getArticle(1L)).thenReturn(article);
        when(usersService.findUserByUsername("unknown")).thenReturn(null);

        DTO result = postRequestsHandler.handleCommentRequest(request);
        assertThat(result).isEqualTo(ArticleDTOEntityConverter.emptyDTO());
    }
}
