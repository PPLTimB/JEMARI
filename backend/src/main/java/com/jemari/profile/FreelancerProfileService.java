package com.jemari.profile;

import com.jemari.common.exception.ResourceNotFoundException;
import com.jemari.profile.dto.FreelancerProfileRequest;
import com.jemari.profile.dto.FreelancerProfileResponse;
import com.jemari.profile.dto.FreelancerSkillResponse;
import com.jemari.profile.dto.FreelancerExperienceResponse;
import com.jemari.profile.entity.FreelancerSkill;
import com.jemari.profile.entity.FreelancerExperience;
import com.jemari.profile.repository.FreelancerSkillRepository;
import com.jemari.profile.repository.FreelancerExperienceRepository;
import com.jemari.user.User;
import com.jemari.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FreelancerProfileService {

    private final FreelancerProfileRepository freelancerProfileRepository;
    private final UserService userService;
    private final FreelancerSkillRepository skillRepository;
    private final FreelancerExperienceRepository experienceRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FreelancerProfileService(FreelancerProfileRepository freelancerProfileRepository,
                                    UserService userService,
                                    FreelancerSkillRepository skillRepository,
                                    FreelancerExperienceRepository experienceRepository) {
        this.freelancerProfileRepository = freelancerProfileRepository;
        this.userService = userService;
        this.skillRepository = skillRepository;
        this.experienceRepository = experienceRepository;
    }

    @Transactional(readOnly = true)
    public FreelancerProfileResponse getMyProfile(Long userId) {
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil freelancer belum dibuat"));
        return toResponse(profile);
    }

    @Transactional
    public FreelancerProfileResponse saveMyProfile(Long userId, FreelancerProfileRequest request) {
        User user = userService.getById(userId);

        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    FreelancerProfile newProfile = new FreelancerProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setFullName(request.getFullName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setBio(request.getBio());
        profile.setHeadline(request.getHeadline());
        profile.setCity(request.getCity());
        profile.setProvince(request.getProvince());
        profile.setAvatarUrl(request.getAvatarUrl());

        FreelancerProfile saved = freelancerProfileRepository.save(profile);
        return toResponse(saved);
    }

    @Transactional
    public void deactivateMyProfile(Long userId) {
        freelancerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil freelancer belum dibuat"));
        userService.deactivate(userId);
    }

    private FreelancerProfileResponse toResponse(FreelancerProfile profile) {
        FreelancerProfileResponse response = new FreelancerProfileResponse();
        response.setId(profile.getId());
        response.setFullName(profile.getFullName());
        response.setPhoneNumber(profile.getPhoneNumber());
        response.setBio(profile.getBio());
        response.setHeadline(profile.getHeadline());
        response.setCity(profile.getCity());
        response.setProvince(profile.getProvince());
        response.setAvatarUrl(profile.getAvatarUrl());
        response.setActive(profile.getUser().isActive());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());

        // Load skills
        List<FreelancerSkillResponse> skills = skillRepository.findByFreelancerProfileIdOrderByCreatedAtAsc(profile.getId())
                .stream()
                .map(this::toSkillResponse)
                .collect(Collectors.toList());
        response.setSkills(skills);

        // Load experiences
        List<FreelancerExperienceResponse> experiences = experienceRepository.findByFreelancerProfileIdOrderByStartDateDesc(profile.getId())
                .stream()
                .map(this::toExperienceResponse)
                .collect(Collectors.toList());
        response.setExperiences(experiences);

        return response;
    }

    private FreelancerSkillResponse toSkillResponse(FreelancerSkill skill) {
        return new FreelancerSkillResponse(
                skill.getId(),
                skill.getSkillName(),
                skill.getSkillLevel(),
                skill.getCreatedAt() != null ? skill.getCreatedAt().format(DATE_FORMATTER) : null,
                skill.getUpdatedAt() != null ? skill.getUpdatedAt().format(DATE_FORMATTER) : null
        );
    }

    private FreelancerExperienceResponse toExperienceResponse(FreelancerExperience experience) {
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