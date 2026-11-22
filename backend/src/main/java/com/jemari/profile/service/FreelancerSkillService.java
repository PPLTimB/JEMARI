package com.jemari.profile.service;

import com.jemari.common.exception.ResourceNotFoundException;
import com.jemari.profile.FreelancerProfile;
import com.jemari.profile.FreelancerProfileRepository;
import com.jemari.profile.dto.FreelancerSkillRequest;
import com.jemari.profile.dto.FreelancerSkillResponse;
import com.jemari.profile.entity.FreelancerSkill;
import com.jemari.profile.repository.FreelancerSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FreelancerSkillService {

    private final FreelancerSkillRepository skillRepository;
    private final FreelancerProfileRepository profileRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FreelancerSkillService(FreelancerSkillRepository skillRepository, FreelancerProfileRepository profileRepository) {
        this.skillRepository = skillRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public List<FreelancerSkillResponse> getMySkills(Long userId) {
        FreelancerProfile profile = getProfileOrThrow(userId);
        return skillRepository.findByFreelancerProfileIdOrderByCreatedAtAsc(profile.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FreelancerSkillResponse addSkill(Long userId, FreelancerSkillRequest request) {
        FreelancerProfile profile = getProfileOrThrow(userId);

        // Validasi unique skill name per profile
        if (skillRepository.existsByFreelancerProfileIdAndSkillName(profile.getId(), request.getSkillName().trim())) {
            throw new IllegalArgumentException("Keahlian dengan nama ini sudah ada");
        }

        FreelancerSkill skill = new FreelancerSkill();
        skill.setFreelancerProfile(profile);
        skill.setSkillName(request.getSkillName().trim());
        skill.setSkillLevel(request.getSkillLevel() != null ? request.getSkillLevel().trim().toUpperCase() : null);

        FreelancerSkill saved = skillRepository.save(skill);
        return toResponse(saved);
    }

    @Transactional
    public FreelancerSkillResponse updateSkill(Long userId, Long skillId, FreelancerSkillRequest request) {
        FreelancerProfile profile = getProfileOrThrow(userId);
        FreelancerSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Keahlian tidak ditemukan"));

        // Validasi ownership
        if (!skill.getFreelancerProfile().getId().equals(profile.getId())) {
            throw new IllegalArgumentException("Akses ditolak: Keahlian bukan milik Anda");
        }

        // Validasi unique skill name (kecuali dirinya sendiri)
        String newSkillName = request.getSkillName().trim();
        if (!skill.getSkillName().equalsIgnoreCase(newSkillName) &&
            skillRepository.existsByFreelancerProfileIdAndSkillName(profile.getId(), newSkillName)) {
            throw new IllegalArgumentException("Keahlian dengan nama ini sudah ada");
        }

        skill.setSkillName(newSkillName);
        skill.setSkillLevel(request.getSkillLevel() != null ? request.getSkillLevel().trim().toUpperCase() : null);

        FreelancerSkill saved = skillRepository.save(skill);
        return toResponse(saved);
    }

    @Transactional
    public void deleteSkill(Long userId, Long skillId) {
        FreelancerProfile profile = getProfileOrThrow(userId);
        FreelancerSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Keahlian tidak ditemukan"));

        // Validasi ownership
        if (!skill.getFreelancerProfile().getId().equals(profile.getId())) {
            throw new IllegalArgumentException("Akses ditolak: Keahlian bukan milik Anda");
        }

        skillRepository.delete(skill);
    }

    private FreelancerProfile getProfileOrThrow(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil freelancer belum dibuat"));
    }

    private FreelancerSkillResponse toResponse(FreelancerSkill skill) {
        return new FreelancerSkillResponse(
                skill.getId(),
                skill.getSkillName(),
                skill.getSkillLevel(),
                skill.getCreatedAt() != null ? skill.getCreatedAt().format(DATE_FORMATTER) : null,
                skill.getUpdatedAt() != null ? skill.getUpdatedAt().format(DATE_FORMATTER) : null
        );
    }
}