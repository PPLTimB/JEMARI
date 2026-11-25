package com.jemari.profile.controller;

import com.jemari.common.dto.ApiResponse;
import com.jemari.profile.dto.FreelancerExperienceRequest;
import com.jemari.profile.dto.FreelancerExperienceResponse;
import com.jemari.profile.service.FreelancerExperienceService;
import com.jemari.user.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/freelancer-profiles/me/experiences")
@PreAuthorize("hasRole('FREELANCER')")
public class FreelancerExperienceController {

    private final FreelancerExperienceService experienceService;

    public FreelancerExperienceController(FreelancerExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FreelancerExperienceResponse>>> getMyExperiences(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<FreelancerExperienceResponse> experiences = experienceService.getMyExperiences(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Daftar pengalaman kerja berhasil dimuat", experiences));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FreelancerExperienceResponse>> addExperience(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FreelancerExperienceRequest request) {
        FreelancerExperienceResponse experience = experienceService.addExperience(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Pengalaman kerja berhasil ditambahkan", experience));
    }

    @PutMapping("/{experienceId}")
    public ResponseEntity<ApiResponse<FreelancerExperienceResponse>> updateExperience(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long experienceId,
            @Valid @RequestBody FreelancerExperienceRequest request) {
        FreelancerExperienceResponse experience = experienceService.updateExperience(principal.getId(), experienceId, request);
        return ResponseEntity.ok(ApiResponse.ok("Pengalaman kerja berhasil diperbarui", experience));
    }

    @DeleteMapping("/{experienceId}")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long experienceId) {
        experienceService.deleteExperience(principal.getId(), experienceId);
        return ResponseEntity.ok(ApiResponse.ok("Pengalaman kerja berhasil dihapus", null));
    }
}