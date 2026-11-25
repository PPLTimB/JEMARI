package com.jemari.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FreelancerSkillRequest {
    @NotBlank(message = "Nama keahlian wajib diisi")
    @Size(max = 100, message = "Nama keahlian maksimal 100 karakter")
    private String skillName;

    @Size(max = 20, message = "Level keahlian maksimal 20 karakter")
    private String skillLevel; // PEMULA, MENENGAH, MAHIR

    public FreelancerSkillRequest() {}

    // --- MANUAL GETTERS & SETTERS ---
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
}