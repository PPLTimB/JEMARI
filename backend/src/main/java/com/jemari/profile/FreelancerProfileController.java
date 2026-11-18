package com.jemari.profile;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jemari.common.dto.ApiResponse;
import com.jemari.profile.dto.FreelancerProfileRequest;
import com.jemari.profile.dto.FreelancerProfileResponse;
import com.jemari.user.UserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/freelancer-profiles")
@PreAuthorize("hasRole('FREELANCER')")
public class FreelancerProfileController {

    private final FreelancerProfileService freelancerProfileService;

    public FreelancerProfileController(FreelancerProfileService freelancerProfileService) {
        this.freelancerProfileService = freelancerProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<FreelancerProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        FreelancerProfileResponse data = freelancerProfileService.getMyProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Profil ditemukan", data));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<FreelancerProfileResponse>> saveMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FreelancerProfileRequest request) {
        FreelancerProfileResponse data = freelancerProfileService.saveMyProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profil berhasil disimpan", data));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deactivateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        freelancerProfileService.deactivateMyProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Profil berhasil dinonaktifkan", null));
    }
}