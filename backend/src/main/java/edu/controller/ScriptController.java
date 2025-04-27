package edu.controller;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scripts")
public class ScriptController {
    @Autowired
    @Setter
    private ResourceLoader resourceLoader;
    private final String mediaType = "text/javascript";

    @GetMapping("/login.js")
    public ResponseEntity<Resource> getLoginScript() {
        Resource resource = resourceLoader.getResource("classpath:UI/static/js/Login.js");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(mediaType))
                .body(resource);
    }

    @GetMapping("/register.js")
    public ResponseEntity<Resource> getRegisterScript() {
        Resource resource = resourceLoader.getResource("classpath:UI/static/js/Register.js");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(mediaType))
                .body(resource);
    }
}
