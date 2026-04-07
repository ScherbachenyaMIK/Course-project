package edu.controller;

import edu.configuration.ApplicationConfig;
import edu.model.web.request.EditProfileRequest;
import edu.model.web.request.ProfileRequest;
import edu.service.ResponseHandler;
import edu.util.AuthenticationChecker;
import edu.web.ScrapperProducer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestController
@RequestMapping("/users")
public class UsersController {
    private static final String REDIRECT_LOGIN_URL = "redirect:/login";

    @Autowired
    private ScrapperProducer scrapperProducer;

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private ResponseHandler responseHandler;

    @GetMapping("/{username}")
    public CompletableFuture<ModelAndView> getProfile(@PathVariable String username) throws NoResourceFoundException {
        String currentUsername = AuthenticationChecker.getCurrentUsername();
        if (currentUsername == null || !currentUsername.equals(username)) {
            throw new NoResourceFoundException(HttpMethod.GET, "/users/" + username);
        }

        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendGetRequest(
                "get_info",
                correlationId,
                new ProfileRequest(
                        username
                ));
        return responseHandler.getResponse(correlationId, AuthenticationChecker.checkAuthorities());
    }

    @PostMapping("/{username}/edit")
    public CompletableFuture<ModelAndView> editProfile(
            @PathVariable String username,
            @RequestParam String nativeName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Character sex,
            @RequestParam(required = false) String birthDate
    ) {
        String currentUsername = AuthenticationChecker.getCurrentUsername();
        if (currentUsername == null || !currentUsername.equals(username)) {
            return CompletableFuture.completedFuture(new ModelAndView(REDIRECT_LOGIN_URL));
        }

        String correlationId = UUID.randomUUID().toString();
        scrapperProducer.sendPostRequest(
                "profile_editing",
                correlationId,
                new EditProfileRequest(
                        username, nativeName, description, sex, birthDate
                ));
        return responseHandler
                .getResponse(correlationId, AuthenticationChecker.checkAuthorities())
                .thenApply(mav -> new ModelAndView("redirect:/users/" + username));
    }
}
