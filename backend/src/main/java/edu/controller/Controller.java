package edu.controller;

import edu.configuration.ApplicationConfig;
import edu.model.web.request.ArticlesForFeedRequest;
import edu.service.ResponseHandler;
import edu.web.ScrapperProducer;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
    private ResponseHandler responseHandler;

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
