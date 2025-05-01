package edu.controller;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/styles")
public class StyleController {
    @Autowired
    @Setter
    private ResourceLoader resourceLoader;

    private final String mediaType = "text/css";

    @GetMapping("/main.css")
    public ResponseEntity<Resource> getMain() {
        Resource resource = resourceLoader.getResource("classpath:UI/static/css/Main.css");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(mediaType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Main.css\"")
                .body(resource);
    }

    @GetMapping("/form.css")
    public ResponseEntity<Resource> getForm() {
        Resource resource = resourceLoader.getResource("classpath:UI/static/css/Form.css");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(mediaType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Form.css\"")
                .body(resource);
    }

    @GetMapping("/home.css")
    public ResponseEntity<Resource> getHome() {
        Resource resource = resourceLoader.getResource("classpath:UI/static/css/Home.css");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(mediaType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Home.css\"")
                .body(resource);
    }

    @GetMapping("/login.css")
    public ResponseEntity<Resource> getLogin() {
        Resource resource = resourceLoader.getResource("classpath:UI/static/css/Login.css");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(mediaType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Login.css\"")
                .body(resource);
    }

    @GetMapping("/register.css")
    public ResponseEntity<Resource> getRegister() {
        Resource resource = resourceLoader.getResource("classpath:UI/static/css/Register.css");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(mediaType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Register.css\"")
                .body(resource);
    }

    @GetMapping("/error.css")
    public ResponseEntity<Resource> getError() {
        Resource resource = resourceLoader.getResource("classpath:UI/static/css/Error.css");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(mediaType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Error.css\"")
                .body(resource);
    }
}
