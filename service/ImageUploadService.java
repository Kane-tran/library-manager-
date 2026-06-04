package com.library.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ImageUploadService {

    // Lưu ảnh vào thư mục static/uploads/covers trong classpath
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/covers/";
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    public String uploadCoverImage(MultipartFile file) throws IOException {
        // Validate
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file selected");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File too large (max 5MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only image files allowed (JPG, PNG, GIF, WEBP)");
        }

        // Generate unique filename
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + ext;

        // Lưu file — thử 2 vị trí: working dir và relative path
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            // Fallback: tạo trong thư mục hiện tại nếu không tìm thấy src
            uploadPath = Paths.get("uploads/covers/");
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Saved cover image: {}", filePath.toAbsolutePath());

        // Trả về URL để lưu vào DB
        return "/uploads/covers/" + filename;
    }
}
