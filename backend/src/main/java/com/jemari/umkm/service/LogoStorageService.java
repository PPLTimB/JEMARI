package com.jemari.umkm.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class LogoStorageService {

    private final Path uploadDirectory =
            Paths.get("uploads").toAbsolutePath().normalize();

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public LogoStorageService() throws IOException {
        Files.createDirectories(uploadDirectory);
    }

    public String storeLogo(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File logo tidak boleh kosong");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                "Ukuran logo maksimal 5 MB"
        );
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Format logo harus JPG, PNG, atau WEBP"
            );
        }

        String originalName = file.getOriginalFilename();
        String extension = "";

        if (originalName != null && originalName.lastIndexOf(".") >= 0) {
            extension = originalName.substring(
                    originalName.lastIndexOf(".")
            ).toLowerCase();
        }

        String fileName = UUID.randomUUID() + extension;

        Path targetLocation = uploadDirectory.resolve(fileName).normalize();

        if (!targetLocation.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Lokasi file tidak valid");
        }

        Files.copy(file.getInputStream(), targetLocation);

        return fileName;
    }

    public void deleteLogo(String fileName) throws IOException {

        if (fileName == null || fileName.isBlank()) {
            return;
        }

        Path filePath = uploadDirectory.resolve(fileName).normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Lokasi file tidak valid");
        }

        Files.deleteIfExists(filePath);
    }
}