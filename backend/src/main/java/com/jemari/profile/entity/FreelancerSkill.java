package com.jemari.profile.entity;

import com.jemari.profile.FreelancerProfile;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "FreelancerSkill")
@Table(name = "freelancer_skills")
public class FreelancerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_profile_id", nullable = false)
    private FreelancerProfile freelancerProfile;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "skill_level", length = 20)
    private String skillLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public FreelancerSkill() {}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- MANUAL GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FreelancerProfile getFreelancerProfile() { return freelancerProfile; }
    public void setFreelancerProfile(FreelancerProfile freelancerProfile) { this.freelancerProfile = freelancerProfile; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}