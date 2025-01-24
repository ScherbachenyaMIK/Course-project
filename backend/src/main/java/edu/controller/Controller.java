package edu.controller;

import edu.service.ScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class Controller {
    @Autowired
    ScrapperService scrapperService;

    @GetMapping("/")
    public ModelAndView getSingIn() {
        return new ModelAndView("Home");
    }
}
