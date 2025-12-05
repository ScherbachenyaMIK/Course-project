package edu.controller;

import edu.configuration.ApplicationConfig;
import edu.model.web.request.ProfileRequest;
import edu.service.ResponseHandler;
import edu.util.AuthenticationChecker;
import edu.web.ScrapperProducer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private ScrapperProducer scrapperProducer;

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private ResponseHandler responseHandler;

    @GetMapping("/{username}")
    public CompletableFuture<ModelAndView> getProfile(@PathVariable String username) throws NoResourceFoundException {
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals(username)) {
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
}
