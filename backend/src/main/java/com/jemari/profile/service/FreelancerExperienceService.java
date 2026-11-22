package com.jemari.profile.service;

import com.jemari.common.exception.ResourceNotFoundException;
import com.jemari.profile.FreelancerProfile;
import com.jemari.profile.FreelancerProfileRepository;
import com.jemari.profile.dto.FreelancerExperienceRequest;
import com.jemari.profile.dto.FreelancerExperienceResponse;
import com.jemari.profile.entity.FreelancerExperience;
import com.jemari.profile.repository.FreelancerExperienceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FreelancerExperienceService {

    private final FreelancerExperienceRepository experienceRepository;
    private final FreelancerProfileRepository profileRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FreelancerExperienceService(FreelancerExperienceRepository experienceRepository, FreelancerProfileRepository profileRepository) {
        this.experienceRepository = experienceRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public List<FreelancerExperienceResponse> getMyExperiences(Long userId) {
        FreelancerProfile profile = getProfileOrThrow(userId);
        return experienceRepository.findByFreelancerProfileIdOrderByStartDateDesc(profile.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FreelancerExperienceResponse addExperience(Long userId, FreelancerExperienceRequest request) {
        FreelancerProfile profile = getProfileOrThrow(userId);

        // Validasi end_date >= start_date
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Tanggal selesai tidak boleh sebelum tanggal mulai");
        }

        FreelancerExperience experience = new FreelancerExperience();
        experience.setFreelancerProfile(profile);
        experience.setPosition(request.getPosition().trim());
        experience.setCompanyName(request.getCompanyName().trim());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        FreelancerExperience saved = experienceRepository.save(experience);
        return toResponse(saved);
    }

    @Transactional
    public FreelancerExperienceResponse updateExperience(Long userId, Long experienceId, FreelancerExperienceRequest request) {
        FreelancerProfile profile = getProfileOrThrow(userId);
        FreelancerExperience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Pengalaman kerja tidak ditemukan"));

        // Validasi ownership
        if (!experience.getFreelancerProfile().getId().equals(profile.getId())) {
            throw new IllegalArgumentException("Akses ditolak: Pengalaman kerja bukan milik Anda");
        }

        // Validasi end_date >= start_date
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Tanggal selesai tidak boleh sebelum tanggal mulai");
        }

        experience.setPosition(request.getPosition().trim());
        experience.setCompanyName(request.getCompanyName().trim());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        FreelancerExperience saved = experienceRepository.save(experience);
        return toResponse(saved);
    }

    @Transactional
    public void deleteExperience(Long userId, Long experienceId) {
        FreelancerProfile profile = getProfileOrThrow(userId);
        FreelancerExperience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Pengalaman kerja tidak ditemukan"));

        // Validasi ownership
        if (!experience.getFreelancerProfile().getId().equals(profile.getId())) {
            throw new IllegalArgumentException("Akses ditolak: Pengalaman kerja bukan milik Anda");
        }

        experienceRepository.delete(experience);
    }

    private FreelancerProfile getProfileOrThrow(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil freelancer belum dibuat"));
    }

    private FreelancerExperienceResponse toResponse(FreelancerExperience experience) {
        return new FreelancerExperienceResponse(
                experience.getId(),
                experience.getPosition(),
                experience.getCompanyName(),
                experience.getStartDate() != null ? experience.getStartDate().toString() : null,
                experience.getEndDate() != null ? experience.getEndDate().toString() : null,
                experience.getDescription(),
                experience.getCreatedAt() != null ? experience.getCreatedAt().format(DATE_FORMATTER) : null,
                experience.getUpdatedAt() != null ? experience.getUpdatedAt().format(DATE_FORMATTER) : null
        );
    }
}