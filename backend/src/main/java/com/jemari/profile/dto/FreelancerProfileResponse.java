package com.jemari.profile.dto;

import java.time.LocalDateTime;
import java.util.List;

public class FreelancerProfileResponse {

    private Long id;
    private String fullName;
    private String phoneNumber;
    private String bio;
    private String headline;
    private String city;
    private String province;
    private String avatarUrl;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FreelancerSkillResponse> skills;
    private List<FreelancerExperienceResponse> experiences;

    public FreelancerProfileResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<FreelancerSkillResponse> getSkills() {
        return skills;
    }

    public void setSkills(List<FreelancerSkillResponse> skills) {
        this.skills = skills;
    }

    public List<FreelancerExperienceResponse> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<FreelancerExperienceResponse> experiences) {
        this.experiences = experiences;
    }
}