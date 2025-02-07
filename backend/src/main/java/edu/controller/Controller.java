package edu.controller;

import edu.configuration.ApplicationConfig;
import edu.configuration.SecurityConfig;
import edu.model.web.request.ArticlesForFeedRequest;
import edu.model.web.request.AuthRequest;
import edu.security.CustomAuthenticationManager;
import edu.security.JwtProvider;
import edu.service.ResponseHandler;
import edu.web.ScrapperProducer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Log4j2
@RestController
public class Controller {
    @Autowired
    private ScrapperProducer scrapperProducer;

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private ResponseHandler responseHandler;

    @Autowired
    CustomAuthenticationManager authenticationManager;

    @Autowired
    JwtProvider jwtProvider;

    @GetMapping("/")
    public CompletableFuture<ModelAndView> getHome() {
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendGetRequest(
                "get_info",
                correlationId,
                new ArticlesForFeedRequest(
                        applicationConfig.initialSearchingCount()
                ));
        return responseHandler.getResponse(correlationId);
    }

    @GetMapping("/login")
    public ModelAndView getLogin() {
        return new ModelAndView("Login");
    }

    @PostMapping("/login")
    public ResponseEntity<?> postLogin(@RequestBody AuthRequest request,
                                                 HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        String token = jwtProvider.generateToken(authentication.getName());
        Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) securityConfig.getExpiration().toSeconds());
        response.addCookie(jwtCookie);
        return ResponseEntity.ok().build();
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
        return modelAndView;
    }
}
