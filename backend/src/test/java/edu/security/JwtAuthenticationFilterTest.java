package edu.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @SneakyThrows
    @Test
    void doFilterInternalEmptyCookie() {
        when(request.getCookies()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(null);

        filter.doFilterInternal(request, response, chain);
        Authentication result =
                SecurityContextHolder.getContext().getAuthentication();

        verify(chain).doFilter(request, response);
        assertThat(result).isNull();
    }

    @SneakyThrows
    @Test
    void doFilterInternal() {
        Authentication expected =
                new UsernamePasswordAuthenticationToken(
                        "test",
                        null,
                        List.of()
                );

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(
                "JWT_TOKEN",
                "test"
        )});
        when(jwtProvider.validateToken(anyString())).thenReturn(true);
        when(jwtProvider.extractUsername(anyString())).thenReturn("test");
        SecurityContextHolder.getContext().setAuthentication(null);

        filter.doFilterInternal(request, response, chain);
        Authentication result =
                SecurityContextHolder.getContext().getAuthentication();

        verify(chain).doFilter(request, response);
        assertThat(result).usingRecursiveComparison().isEqualTo(expected);
    }

    @SneakyThrows
    @Test
    void doFilterInternalBadToken() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(
                "JWT_TOKEN",
                "test"
        )});
        when(jwtProvider.validateToken(anyString())).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(null);

        filter.doFilterInternal(request, response, chain);
        Authentication result =
                SecurityContextHolder.getContext().getAuthentication();

        verify(chain).doFilter(request, response);
        assertThat(result).isNull();
    }
}