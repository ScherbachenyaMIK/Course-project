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
@RequestMapping("/resources")
public class ResourceController {
    @Autowired
    @Setter
    private ResourceLoader resourceLoader;

    @GetMapping("/icon.svg")
    public ResponseEntity<Resource> getIcon() {
        MediaType mediaType = new MediaType("image", "svg+xml");
        Resource resource = resourceLoader.getResource("classpath:UI/resources/Icon.svg");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Icon.svg\"")
                .body(resource);
    }

    @GetMapping("/icon_p.png")
    public ResponseEntity<Resource> getIconPink() {
        Resource resource = resourceLoader.getResource("classpath:UI/resources/Icon_p.png");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Icon_p.png\"")
                .body(resource);
    }

    @GetMapping("/sign_up_icon.png")
    public ResponseEntity<Resource> getSignUpIconPink() {
        Resource resource = resourceLoader.getResource("classpath:UI/resources/Sign_up_icon.png");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Sign_up_icon.png\"")
                .body(resource);
    }

    @GetMapping("/sign_up_icon_g.png")
    public ResponseEntity<Resource> getSignUpIconGreen() {
        Resource resource = resourceLoader.getResource("classpath:UI/resources/Sign_up_icon_g.png");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Sign_up_icon_g.png\"")
                .body(resource);
    }

    @GetMapping("/log_in_icon.png")
    public ResponseEntity<Resource> getLogInIconPink() {
        Resource resource = resourceLoader.getResource("classpath:UI/resources/Log_in_icon.png");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Log_in_icon.png\"")
                .body(resource);
    }

    @GetMapping("/log_in_icon_g.png")
    public ResponseEntity<Resource> getLogInIconGreen() {
        Resource resource = resourceLoader.getResource("classpath:UI/resources/Log_in_icon_g.png");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Log_in_icon_g.png\"")
                .body(resource);
    }

    @GetMapping("/magnifier.png")
    public ResponseEntity<Resource> getMagnifier() {
        Resource resource = resourceLoader.getResource("classpath:UI/resources/Magnifier.png");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Magnifier.png\"")
                .body(resource);
    }

    @GetMapping("/standard-preview.png")
    public ResponseEntity<Resource> getStandardPreview() {
        Resource resource = resourceLoader.getResource("classpath:UI/resources/Logo_draw.png");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Logo_draw.png\"")
                .body(resource);
    }
}
