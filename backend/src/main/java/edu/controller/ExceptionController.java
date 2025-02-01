package edu.controller;

import edu.util.StatusCodeDescriptor;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ExceptionController {
    @Autowired
    private StatusCodeDescriptor statusCodeDescriptor;

    private final String redirectUrl = "redirect:/checked-error";

    private final String httpAttribute = "HttpCode";

    private final String descriptionAttribute = "ResponseDescription";

    @ExceptionHandler(TimeoutException.class)
    public ModelAndView handleTimeout() {
        ModelAndView modelAndView = new ModelAndView(redirectUrl);
        modelAndView.addObject(httpAttribute, HttpStatus.REQUEST_TIMEOUT.value());
        modelAndView.addObject(descriptionAttribute,
                statusCodeDescriptor.getDescription(HttpStatus.REQUEST_TIMEOUT.value())
        );
        return modelAndView;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNotFound() {
        ModelAndView modelAndView = new ModelAndView(redirectUrl);
        modelAndView.addObject(httpAttribute, HttpStatus.NOT_FOUND.value());
        modelAndView.addObject(descriptionAttribute,
                statusCodeDescriptor.getDescription(HttpStatus.NOT_FOUND.value())
        );
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnknownException() {
        return new ModelAndView(redirectUrl);
    }
}
