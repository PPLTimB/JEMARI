package com.jemari.profile.controller;

import com.jemari.common.dto.ApiResponse;
import com.jemari.profile.dto.FreelancerSkillRequest;
import com.jemari.profile.dto.FreelancerSkillResponse;
import com.jemari.profile.service.FreelancerSkillService;
import com.jemari.user.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/freelancer-profiles/me/skills")
@PreAuthorize("hasRole('FREELANCER')")
public class FreelancerSkillController {

    private final FreelancerSkillService skillService;

    public FreelancerSkillController(FreelancerSkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FreelancerSkillResponse>>> getMySkills(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<FreelancerSkillResponse> skills = skillService.getMySkills(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Daftar keahlian berhasil dimuat", skills));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FreelancerSkillResponse>> addSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FreelancerSkillRequest request) {
        FreelancerSkillResponse skill = skillService.addSkill(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Keahlian berhasil ditambahkan", skill));
    }

    @PutMapping("/{skillId}")
    public ResponseEntity<ApiResponse<FreelancerSkillResponse>> updateSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long skillId,
            @Valid @RequestBody FreelancerSkillRequest request) {
        FreelancerSkillResponse skill = skillService.updateSkill(principal.getId(), skillId, request);
        return ResponseEntity.ok(ApiResponse.ok("Keahlian berhasil diperbarui", skill));
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long skillId) {
        skillService.deleteSkill(principal.getId(), skillId);
        return ResponseEntity.ok(ApiResponse.ok("Keahlian berhasil dihapus", null));
    }
}