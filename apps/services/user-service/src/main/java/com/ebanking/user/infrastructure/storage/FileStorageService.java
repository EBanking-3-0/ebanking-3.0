package com.ebanking.user.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.file-storage.path:./uploads}")
    private String storagePath;

    /**
     * Stores a file and returns the relative URL path
     */
    public String storeFile(MultipartFile file, String userId, String documentType) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Create directory structure: uploads/{userId}/{documentType}/
        Path userDir = Paths.get(storagePath, userId, documentType);
        Files.createDirectories(userDir);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID() + extension;

        // Save file
        Path targetPath = userDir.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative URL path
        String relativePath = String.format("/uploads/%s/%s/%s", userId, documentType, filename);
        log.info("File stored at: {}", relativePath);
        return relativePath;
    }

    /**
     * Stores a base64 encoded image and returns the relative URL path
     */
    public String storeBase64Image(String base64Image, String userId, String documentType) throws IOException {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new IllegalArgumentException("Base64 image is empty");
        }

        // Remove data URL prefix if present
        String base64Data = base64Image;
        String extension = ".jpg";
        if (base64Image.startsWith("data:image/")) {
            int commaIndex = base64Image.indexOf(",");
            if (commaIndex > 0) {
                String mimeType = base64Image.substring(5, commaIndex);
                if (mimeType.contains("png")) {
                    extension = ".png";
                } else if (mimeType.contains("jpeg") || mimeType.contains("jpg")) {
                    extension = ".jpg";
                }
                base64Data = base64Image.substring(commaIndex + 1);
            }
        }

        // Decode base64
        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

        // Create directory structure
        Path userDir = Paths.get(storagePath, userId, documentType);
        Files.createDirectories(userDir);

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path targetPath = userDir.resolve(filename);
        Files.write(targetPath, imageBytes);

        // Return relative URL path
        String relativePath = String.format("/uploads/%s/%s/%s", userId, documentType, filename);
        log.info("Base64 image stored at: {}", relativePath);
        return relativePath;
    }

    /**
     * Deletes a file
     */
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(storagePath).resolve(filePath.replaceFirst("/uploads/", ""));
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("File deleted: {}", filePath);
        }
    }
}

