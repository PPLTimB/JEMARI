package com.jemari.umkm;

import com.jemari.common.dto.ApiResponse;
import com.jemari.common.exception.ResourceNotFoundException;
import com.jemari.umkm.dto.UmkmProfileRequest;
import com.jemari.umkm.dto.UmkmProfileResponse;
import com.jemari.user.User;
import com.jemari.user.UserRepository;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.IOException;

@RestController
@RequestMapping("/api/umkm-profiles")
@PreAuthorize("hasRole('UMKM')")
public class UmkmProfileController {

    private final UmkmProfileService umkmProfileService;
    private final UserRepository userRepository;

    public UmkmProfileController(
            UmkmProfileService umkmProfileService,
            UserRepository userRepository) {
        this.umkmProfileService = umkmProfileService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pengguna tidak ditemukan"
                ));

        return user.getId();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UmkmProfileResponse>> createMyProfile(
            Authentication authentication,
            @Valid @RequestBody UmkmProfileRequest request) {

        UmkmProfileResponse data = umkmProfileService.createMyProfile(
                getCurrentUserId(authentication), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Profil usaha berhasil dibuat", data));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UmkmProfileResponse>> getMyProfile(
            Authentication authentication) {

        UmkmProfileResponse data = umkmProfileService.getMyProfile(
                getCurrentUserId(authentication));

        return ResponseEntity.ok(
                ApiResponse.ok("Profil usaha ditemukan", data));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UmkmProfileResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UmkmProfileRequest request) {

        UmkmProfileResponse data = umkmProfileService.updateMyProfile(
                getCurrentUserId(authentication), request);

        return ResponseEntity.ok(
                ApiResponse.ok("Profil usaha berhasil diperbarui", data));
    }

    @PostMapping(value = "/me/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UmkmProfileResponse>> uploadLogo(
        Authentication authentication,
        @RequestParam("file") MultipartFile file) throws IOException {
      UmkmProfileResponse data = umkmProfileService.uploadLogo(
            getCurrentUserId(authentication), file);

      return ResponseEntity.ok(
            ApiResponse.ok("Logo profil berhasil diunggah", data));
    }

    @DeleteMapping("/me/logo")
    public ResponseEntity<ApiResponse<UmkmProfileResponse>> deleteLogo(
        Authentication authentication) throws IOException {

        UmkmProfileResponse data = umkmProfileService.deleteLogo(
            getCurrentUserId(authentication));

        return ResponseEntity.ok(
            ApiResponse.ok("Logo profil berhasil dihapus", data));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteMyProfile(
            Authentication authentication) {

        umkmProfileService.deleteMyProfile(
                getCurrentUserId(authentication));

        return ResponseEntity.ok(
                ApiResponse.ok("Profil usaha berhasil dihapus", null));
    }
}