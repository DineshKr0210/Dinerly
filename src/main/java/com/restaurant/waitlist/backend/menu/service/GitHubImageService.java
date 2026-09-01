package com.restaurant.waitlist.backend.menu.service;

import org.springframework.web.multipart.MultipartFile;

public interface GitHubImageService {
    String uploadImage(MultipartFile file);
}
