package edu.controller;

import edu.configuration.ApplicationConfig;
import edu.configuration.SecurityConfig;
import edu.model.web.request.ArticleSearchRequest;
import edu.model.web.request.ArticlesForFeedRequest;
import edu.model.web.request.ConfirmEmailRequest;
import edu.model.web.request.LoginRequest;
import edu.model.web.request.RegisterRequest;
import edu.model.web.response.AuthenticationResponse;
import edu.model.web.response.ConfirmEmailResponse;
import edu.model.web.response.RegisterResponse;
import edu.security.CustomAuthenticationManager;
import edu.security.JwtProvider;
import edu.service.EmailService;
import edu.service.ResponseHandler;
import edu.util.AuthenticationChecker;
import edu.web.ScrapperProducer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class Controller {
    private static final String REDIRECT_HOME_URL = "redirect:/";

    private static final String JWT_TOKEN_NAME = "JWT_TOKEN";

    private static final String GET_INFO_TOPIC = "get_info";

    @Autowired
    private ScrapperProducer scrapperProducer;

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private ResponseHandler responseHandler;

    @Autowired
    private AuthorizationListener authorizationListener;

    @Autowired
    private CustomAuthenticationManager authenticationManager;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public CompletableFuture<ModelAndView> getHome() {
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendGetRequest(
                GET_INFO_TOPIC,
                correlationId,
                new ArticlesForFeedRequest(
                        applicationConfig.initialSearchingCount()
                ));
        return responseHandler.getResponse(correlationId, AuthenticationChecker.checkAuthorities());
    }

    @SuppressWarnings("ParameterNumber")
    @GetMapping("/search")
    public CompletableFuture<ModelAndView> getSearch(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "minLikes", required = false) Integer minLikes,
            @RequestParam(value = "minViews", required = false) Integer minViews,
            @RequestParam(value = "minComments", required = false) Integer minComments,
            @RequestParam(value = "sort", required = false, defaultValue = "relevance") String sort,
            @RequestParam(value = "limit", required = false, defaultValue = "20") int limit
    ) {
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendGetRequest(
                GET_INFO_TOPIC,
                correlationId,
                new ArticleSearchRequest(
                        query == null ? "" : query,
                        minLikes, minViews, minComments,
                        sort, limit
                ));
        CompletableFuture<ModelAndView> future = responseHandler.getResponse(
                correlationId, AuthenticationChecker.checkAuthorities());
        return future.thenApply(mav -> {
            if (mav == null) {
                return null;
            }
            mav.addObject("query", query == null ? "" : query);
            mav.addObject("minLikes", minLikes);
            mav.addObject("minViews", minViews);
            mav.addObject("minComments", minComments);
            mav.addObject("sort", sort);
            mav.addObject("limit", limit);
            return mav;
        });
    }

    @GetMapping("/login")
    public ModelAndView getLogin(
            @RequestParam(value = "fragment", required = false) Boolean fragment
    ) {
        if (AuthenticationChecker.checkAuthorities()) {
            return new ModelAndView(REDIRECT_HOME_URL);
        }

        return Boolean.TRUE.equals(fragment)
                ? new ModelAndView("Login :: login-form")
                : new ModelAndView("Login");
    }

    @GetMapping("/register")
    public ModelAndView getRegister(
            @RequestParam(value = "fragment", required = false) Boolean fragment
    ) {
        if (AuthenticationChecker.checkAuthorities()) {
            return new ModelAndView(REDIRECT_HOME_URL);
        }

        return Boolean.TRUE.equals(fragment)
                ? new ModelAndView("Register :: register-form")
                : new ModelAndView("Register");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> postLogin(@RequestBody LoginRequest request,
                                                            HttpServletResponse response) {
        AuthenticationResponse result = new AuthenticationResponse();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            Cookie jwtCookie = prepareCookie(authentication);
            response.addCookie(jwtCookie);
        } catch (BadCredentialsException e) {
            result.setSuccess(false);
            result.setCause("BadCredentialsException");
        } catch (AuthenticationServiceException e) {
            result.setSuccess(false);
            result.setCause("AuthenticationServiceException");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    public ResponseEntity<?> postRegister(@RequestBody RegisterRequest request) {
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendAuthRequest(correlationId, request);
        RegisterResponse response;
        try {
            response = (RegisterResponse) authorizationListener.waitForResponse(correlationId);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            response = new RegisterResponse(false);
        }
        if (response.success()) {
            emailService.sendConfirmationEmail(request.username(), request.email());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/confirm")
    public ModelAndView confirmEmail(@RequestParam("token") String token) {
        String errorUrl = "redirect:/checked-error?HttpCode=400";
        if (!jwtProvider.validateToken(token)
                || !JwtProvider.PURPOSE_EMAIL_CONFIRM.equals(jwtProvider.extractPurpose(token))) {
            return new ModelAndView(errorUrl
                    + "&ResponseDescription=Invalid+or+expired+confirmation+link");
        }
        String username = jwtProvider.extractUsername(token);
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendAuthRequest(correlationId, new ConfirmEmailRequest(username));
        ConfirmEmailResponse response;
        try {
            response = (ConfirmEmailResponse) authorizationListener.waitForResponse(correlationId);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            response = new ConfirmEmailResponse(false);
        }
        if (response == null || !response.success()) {
            return new ModelAndView(errorUrl
                    + "&ResponseDescription=Email+confirmation+failed");
        }
        return new ModelAndView("redirect:/login");
    }

    @DeleteMapping("/login")
    public ModelAndView deleteLogin(HttpServletResponse response) {
        Cookie jwtCookie = prepareDeleteCookie();
        response.addCookie(jwtCookie);
        return new ModelAndView(REDIRECT_HOME_URL);
    }

    @GetMapping("/authorized")
    public String getPage() {
        return "Success";
    }

    @SuppressWarnings({"ParameterName", "MagicNumber"})
    @RequestMapping("/checked-error")
    public ModelAndView handleError(@RequestParam(required = false) Integer HttpCode,
                              @RequestParam(required = false) String ResponseDescription) {
        ModelAndView modelAndView = new ModelAndView("Error");
        modelAndView.addObject("HttpCode",
                HttpCode != null ? HttpCode : 500);
        modelAndView.addObject("ResponseDescription",
                ResponseDescription != null ? ResponseDescription : "Неизвестная ошибка.");
        modelAndView.addObject(
                "isAuthenticated",
                AuthenticationChecker.checkAuthorities()
        );
        return modelAndView;
    }

    private Cookie prepareCookie(Authentication authentication) {
        String rolePrefix = "ROLE_";
        String role = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith(rolePrefix))
                .map(a -> a.substring(rolePrefix.length()))
                .findFirst()
                .orElse("USER");
        String token = jwtProvider.generateToken(authentication.getName(), role);
        Cookie jwtCookie = new Cookie(JWT_TOKEN_NAME, token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) securityConfig.getExpiration().toSeconds());
        return jwtCookie;
    }

    private Cookie prepareDeleteCookie() {
        Cookie jwtCookie = new Cookie(JWT_TOKEN_NAME, "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        return jwtCookie;
    }
}
