package com.jemari.umkm;

import com.jemari.umkm.service.LogoStorageService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.jemari.common.exception.AppException;
import com.jemari.common.exception.ResourceNotFoundException;
import com.jemari.umkm.dto.UmkmProfileRequest;
import com.jemari.umkm.dto.UmkmProfileResponse;
import com.jemari.user.User;
import com.jemari.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UmkmProfileService {

    private final LogoStorageService logoStorageService;
    private final UmkmProfileRepository umkmProfileRepository;
    private final UserRepository userRepository;

    public UmkmProfileService(UmkmProfileRepository umkmProfileRepository,
                          UserRepository userRepository,
                          LogoStorageService logoStorageService) {
        this.umkmProfileRepository = umkmProfileRepository;
        this.userRepository = userRepository;
        this.logoStorageService = logoStorageService;
    }

    @Transactional(readOnly = true)
    public UmkmProfileResponse getMyProfile(Long userId) {
        return toResponse(findActiveProfile(userId));
    }

    @Transactional
    public UmkmProfileResponse createMyProfile(Long userId, UmkmProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pengguna tidak ditemukan"));

        UmkmProfile profile = umkmProfileRepository.findByUserId(userId).orElse(null);

        if (profile != null && profile.isActive()) {
            throw new AppException(
                    "Profil usaha sudah dibuat, gunakan perubahan profil",
                    HttpStatus.CONFLICT
            );
        }

        if (profile == null) {
            profile = new UmkmProfile();
            profile.setUser(user);
        }

        applyRequest(profile, request);
        profile.setActive(true);

        return toResponse(umkmProfileRepository.save(profile));
    }

    @Transactional
    public UmkmProfileResponse updateMyProfile(Long userId, UmkmProfileRequest request) {
        UmkmProfile profile = findActiveProfile(userId);
        applyRequest(profile, request);

        return toResponse(umkmProfileRepository.save(profile));
    }


    @Transactional
    public UmkmProfileResponse uploadLogo(Long userId, MultipartFile file)
        throws IOException {

        UmkmProfile profile = findActiveProfile(userId);

        // Simpan logo baru
        String newLogoFileName = logoStorageService.storeLogo(file);

        // Simpan nama logo lama untuk dihapus
        String oldLogoUrl = profile.getLogoUrl();

        // Simpan nama file logo baru ke database
        profile.setLogoUrl(newLogoFileName);

        UmkmProfile savedProfile = umkmProfileRepository.save(profile);

        // Hapus logo lama setelah logo baru berhasil disimpan
        if (oldLogoUrl != null && !oldLogoUrl.isBlank()) {
            logoStorageService.deleteLogo(oldLogoUrl);
        }

        return toResponse(savedProfile);
    }

    @Transactional
    public UmkmProfileResponse deleteLogo(Long userId) throws IOException {

        UmkmProfile profile = findActiveProfile(userId);

        String oldLogoUrl = profile.getLogoUrl();

        if (oldLogoUrl == null || oldLogoUrl.isBlank()) {
        throw new AppException(
                "Profil usaha belum memiliki logo",
                HttpStatus.BAD_REQUEST
        );
    }

    profile.setLogoUrl(null);

    UmkmProfile savedProfile = umkmProfileRepository.save(profile);

    logoStorageService.deleteLogo(oldLogoUrl);

    return toResponse(savedProfile);
}

    @Transactional
    public void deleteMyProfile(Long userId) {
        UmkmProfile profile = findActiveProfile(userId);
        profile.setActive(false);
        umkmProfileRepository.save(profile);
    }

    private UmkmProfile findActiveProfile(Long userId) {
        return umkmProfileRepository.findByUserId(userId)
                .filter(UmkmProfile::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profil usaha belum dibuat"
                ));
    }

    private void applyRequest(UmkmProfile profile, UmkmProfileRequest request) {
        profile.setBusinessName(request.getBusinessName().trim());
        profile.setDescription(request.getDescription());
        profile.setCategory(request.getCategory().trim());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setContactEmail(request.getContactEmail());
    }

    private UmkmProfileResponse toResponse(UmkmProfile profile) {
        UmkmProfileResponse response = new UmkmProfileResponse();
        response.setId(profile.getId());
        response.setBusinessName(profile.getBusinessName());
        response.setDescription(profile.getDescription());
        response.setCategory(profile.getCategory());
        response.setPhoneNumber(profile.getPhoneNumber());
        response.setContactEmail(profile.getContactEmail());
        response.setLogoUrl(profile.getLogoUrl());
        response.setActive(profile.isActive());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());

        return response;
    }
}