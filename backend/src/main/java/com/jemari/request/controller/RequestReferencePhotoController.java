package com.jemari.request.controller;

import com.jemari.request.dto.RequestReferencePhotoResponse;
import com.jemari.request.entity.RequestReferencePhoto;
import com.jemari.request.repository.RequestReferencePhotoRepository;
import com.jemari.request.service.RequestReferencePhotoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/requests/{requestId}/reference-photos")
public class RequestReferencePhotoController {

    private final RequestReferencePhotoService photoService;
    private final RequestReferencePhotoRepository photoRepository;

    // Constructor manual (Pengganti @RequiredArgsConstructor)
    public RequestReferencePhotoController(RequestReferencePhotoService photoService, RequestReferencePhotoRepository photoRepository) {
        this.photoService = photoService;
        this.photoRepository = photoRepository;
    }

    @Value("${app.storage.request-reference-dir}")
    private String storageDir;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RequestReferencePhotoResponse> upload(
            @PathVariable Long requestId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(photoService.uploadPhoto(requestId, file));
    }

    @GetMapping
    public ResponseEntity<List<RequestReferencePhotoResponse>> getPhotos(@PathVariable Long requestId) {
        return ResponseEntity.ok(photoService.getPhotos(requestId));
    }

    @PutMapping(value = "/{photoId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RequestReferencePhotoResponse> replace(
            @PathVariable Long requestId,
            @PathVariable Long photoId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(photoService.replacePhoto(requestId, photoId, file));
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> delete(@PathVariable Long requestId, @PathVariable Long photoId) {
        photoService.deletePhoto(requestId, photoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/file/{fileName:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable Long requestId, @PathVariable String fileName) {
        try {
            Path file = Paths.get(storageDir).resolve(requestId + "/" + fileName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("File tidak ditemukan");
            }
        } catch (Exception e) {
            throw new RuntimeException("Gagal membaca file: " + e.getMessage());
        }
    }
}