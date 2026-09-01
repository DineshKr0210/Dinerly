package com.restaurant.waitlist.backend.menu.impl;

import com.restaurant.waitlist.backend.menu.service.GitHubImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GitHubImageServiceImpl implements GitHubImageService {

    @Value("${github.token:}")
    private String token;

    @Value("${github.owner:}")
    private String owner;

    @Value("${github.repo:}")
    private String repo;

    @Value("${github.branch:main}")
    private String branch;

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            String safeName = UUID.randomUUID() + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");

            String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + safeName;

            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            Map<String, Object> body = new HashMap<>();
            body.put("message", "Upload dish image");
            body.put("content", base64);
            body.put("branch", branch);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.put(apiUrl, request);

            return "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + branch + "/" + safeName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to GitHub", e);
        }
    }
}
