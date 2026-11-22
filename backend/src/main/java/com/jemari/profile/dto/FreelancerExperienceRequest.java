package com.jemari.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class FreelancerExperienceRequest {
    @NotBlank(message = "Posisi/jabatan wajib diisi")
    @Size(max = 150, message = "Posisi maksimal 150 karakter")
    private String position;

    @NotBlank(message = "Nama perusahaan wajib diisi")
    @Size(max = 150, message = "Nama perusahaan maksimal 150 karakter")
    private String companyName;

    @NotNull(message = "Tanggal mulai wajib diisi")
    private LocalDate startDate;

    private LocalDate endDate; // null = masih berjalan

    @Size(max = 1000, message = "Deskripsi maksimal 1000 karakter")
    private String description;

    public FreelancerExperienceRequest() {}

    // --- MANUAL GETTERS & SETTERS ---
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}