package com.jemari.request.service;

import com.jemari.request.ServiceRequest;
import com.jemari.request.RequestRepository;
import com.jemari.request.dto.RequestReferencePhotoResponse;
import com.jemari.request.entity.RequestReferencePhoto;
import com.jemari.request.repository.RequestReferencePhotoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RequestReferencePhotoService {

    private final RequestReferencePhotoRepository photoRepository;
    private final RequestRepository requestRepository;

    // Constructor manual (Pengganti @RequiredArgsConstructor)
    public RequestReferencePhotoService(RequestReferencePhotoRepository photoRepository, RequestRepository requestRepository) {
        this.photoRepository = photoRepository;
        this.requestRepository = requestRepository;
    }

    @Value("${app.storage.request-reference-dir}")
    private String storageDir;

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final int MAX_PHOTOS = 5;

    @Transactional
    public RequestReferencePhotoResponse uploadPhoto(Long requestId, MultipartFile file) {
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request tidak ditemukan"));

        long currentCount = photoRepository.countByRequestId(requestId);
        if (currentCount >= MAX_PHOTOS) {
            throw new RuntimeException("Maksimal " + MAX_PHOTOS + " foto referensi per request.");
        }

        validateFile(file);
        String savedPath = saveFileToDisk(requestId, file);

        // Manual Builder
        RequestReferencePhoto photo = new RequestReferencePhoto();
        photo.setRequest(request);
        photo.setFilePath(savedPath);
        photo.setPhotoUrl("/api/requests/" + requestId + "/reference-photos/file/" + savedPath);
        photo.setOriginalName(file.getOriginalFilename());
        photo.setContentType(file.getContentType());
        photo.setFileSize(file.getSize());
        photo.setSortOrder((int) currentCount + 1);

        return mapToResponse(photoRepository.save(photo));
    }

    @Transactional
    public RequestReferencePhotoResponse replacePhoto(Long requestId, Long photoId, MultipartFile newFile) {
        RequestReferencePhoto photo = getPhotoAndValidateOwnership(requestId, photoId);
        validateFile(newFile);

        try {
            Path oldFilePath = Paths.get(storageDir).resolve(photo.getFilePath());
            Files.deleteIfExists(oldFilePath);
        } catch (IOException e) {
            System.err.println("Gagal hapus file lama: " + e.getMessage());
        }

        String newSavedPath = saveFileToDisk(requestId, newFile);
        photo.setFilePath(newSavedPath);
        photo.setPhotoUrl("/api/requests/" + requestId + "/reference-photos/file/" + newSavedPath);
        photo.setOriginalName(newFile.getOriginalFilename());
        photo.setContentType(newFile.getContentType());
        photo.setFileSize(newFile.getSize());

        return mapToResponse(photoRepository.save(photo));
    }

    @Transactional
    public void deletePhoto(Long requestId, Long photoId) {
        RequestReferencePhoto photo = getPhotoAndValidateOwnership(requestId, photoId);
        
        try {
            Path filePath = Paths.get(storageDir).resolve(photo.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Gagal hapus file fisik: " + e.getMessage());
        }

        photoRepository.delete(photo);
    }

    public List<RequestReferencePhotoResponse> getPhotos(Long requestId) {
        return photoRepository.findByRequestIdOrderBySortOrderAsc(requestId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("File kosong");
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Format harus JPG, PNG, atau WEBP");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Ukuran file maksimal 5MB");
        }
    }

    private String saveFileToDisk(Long requestId, MultipartFile file) {
        try {
            Path requestDir = Paths.get(storageDir, String.valueOf(requestId));
            Files.createDirectories(requestDir);
            
            String extension = getExtension(file.getOriginalFilename());
            String newFileName = UUID.randomUUID().toString() + "." + extension;
            Path targetPath = requestDir.resolve(newFileName);
            
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return requestId + "/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file: " + e.getMessage());
        }
    }

    private RequestReferencePhoto getPhotoAndValidateOwnership(Long requestId, Long photoId) {
        RequestReferencePhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Foto tidak ditemukan"));
        if (!photo.getRequest().getId().equals(requestId)) {
            throw new RuntimeException("Akses ditolak: Foto bukan milik request ini");
        }
        return photo;
    }

    private RequestReferencePhotoResponse mapToResponse(RequestReferencePhoto photo) {
        // Manual Builder
        return new RequestReferencePhotoResponse(
                photo.getId(),
                photo.getPhotoUrl(),
                photo.getOriginalName(),
                photo.getSortOrder()
        );
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}