package com.jemari.profile.dto;

public class FreelancerSkillResponse {
    private Long id;
    private String skillName;
    private String skillLevel;
    private String createdAt;
    private String updatedAt;

    public FreelancerSkillResponse() {}

    public FreelancerSkillResponse(Long id, String skillName, String skillLevel, String createdAt, String updatedAt) {
        this.id = id;
        this.skillName = skillName;
        this.skillLevel = skillLevel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- MANUAL GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}