package com.jemari.profile;

import com.jemari.common.exception.ResourceNotFoundException;
import com.jemari.profile.dto.FreelancerProfileRequest;
import com.jemari.profile.dto.FreelancerProfileResponse;
import com.jemari.user.User;
import com.jemari.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FreelancerProfileService {

    private final FreelancerProfileRepository freelancerProfileRepository;
    private final UserService userService;

    public FreelancerProfileService(FreelancerProfileRepository freelancerProfileRepository,
                                    UserService userService) {
        this.freelancerProfileRepository = freelancerProfileRepository;
        this.userService = userService;
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
        return response;
    }
}