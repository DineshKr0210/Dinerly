package com.restaurant.waitlist.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerUiController {

    @GetMapping(value = {"/swagger-ui.html", "/swagger-ui/index.html"})
    public ResponseEntity<Resource> customSwaggerUi() {
        Resource resource = new ClassPathResource("static/swagger-ui/index.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(resource);
    }

    @GetMapping("/swagger-ui/swagger-theme.css")
    public ResponseEntity<Resource> customSwaggerThemeCss() {
        Resource resource = new ClassPathResource("static/swagger-ui/swagger-theme.css");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/css"))
                .body(resource);
    }

    @GetMapping("/swagger-ui/swagger-theme.js")
    public ResponseEntity<Resource> customSwaggerThemeJs() {
        Resource resource = new ClassPathResource("static/swagger-ui/swagger-theme.js");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/javascript"))
                .body(resource);
    }

    @GetMapping("/swagger-ui/favicon.svg")
    public ResponseEntity<Resource> customSwaggerFavicon() {
        Resource resource = new ClassPathResource("static/swagger-ui/favicon.svg");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(resource);
    }
}
