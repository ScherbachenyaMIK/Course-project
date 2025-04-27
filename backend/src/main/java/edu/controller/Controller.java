package edu.controller;

import edu.configuration.ApplicationConfig;
import edu.configuration.SecurityConfig;
import edu.model.web.request.ArticlesForFeedRequest;
import edu.model.web.request.LoginRequest;
import edu.model.web.request.RegisterRequest;
import edu.security.CustomAuthenticationManager;
import edu.security.JwtProvider;
import edu.service.ResponseHandler;
import edu.web.ScrapperProducer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

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
    private CustomAuthenticationManager authenticationManager;

    @Autowired
    private JwtProvider jwtProvider;

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

    @GetMapping("/register")
    public ModelAndView getRegister(Model model) {
        model.addAttribute(
                "registerRequest",
                new RegisterRequest(
                        "",
                        "",
                        "",
                        "",
                        null,
                        null
                )
        );
        return new ModelAndView("Register");
    }

    @PostMapping("/login")
    public ResponseEntity<?> postLogin(@RequestBody LoginRequest request,
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

    @PostMapping("/register")
    public ResponseEntity<?> postRegister(@ModelAttribute RegisterRequest request,
                                          BindingResult result) {
        if (result.hasErrors()) {
            throw new BadCredentialsException("Some fields was corrupted");
        }
        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendAuthRequest(correlationId, request);
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
