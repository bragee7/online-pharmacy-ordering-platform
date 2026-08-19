package com.medicare.pharmacy.service;

import com.medicare.pharmacy.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private final long maxSizeBytes;

    public FileStorageService(
            @Value("${app.upload.dir:uploads}") String uploadDirStr,
            @Value("${app.upload.max-size-mb:5}") long maxSizeMb) {
        this.uploadDir = Paths.get(uploadDirStr).toAbsolutePath().normalize();
        this.maxSizeBytes = maxSizeMb * 1024 * 1024;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDir);
            Files.createDirectories(uploadDir.resolve("prescriptions"));
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create upload directories", ex);
        }
    }

    public String store(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new BadRequestException("File too large. Max size: " + (maxSizeBytes / (1024 * 1024)) + " MB");
        }
        String original = file.getOriginalFilename();
        String ext = getExtension(original).toLowerCase(Locale.ROOT);
        Set<String> allowedExt = Set.of("pdf", "jpg", "jpeg", "png");
        if (!allowedExt.contains(ext)) {
            throw new BadRequestException("Invalid file type. Allowed: pdf, jpg, jpeg, png");
        }
        String contentType = file.getContentType();
        Set<String> allowedTypes = Set.of(
                "application/pdf", "image/jpeg", "image/png", "image/jpg", "application/octet-stream");
        if (contentType != null && !allowedTypes.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Invalid content type: " + contentType);
        }
        String prefix = userId != null ? String.valueOf(userId) : "anon";
        String filename = prefix + "_" + UUID.randomUUID() + "." + ext;
        try {
            Path targetDir = uploadDir.resolve("prescriptions");
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "prescriptions/" + filename;
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store file: " + ex.getMessage());
        }
    }

    public Resource loadAsResource(String storedPath) {
        try {
            Path filePath = uploadDir.resolve(storedPath).normalize();
            UrlResource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BadRequestException("File not found: " + storedPath);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new BadRequestException("File not found: " + storedPath);
        }
    }

    public void delete(String storedPath) {
        try {
            if (storedPath == null || storedPath.isBlank()) {
                return;
            }
            Path filePath = uploadDir.resolve(storedPath).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // best-effort delete
        }
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
