package com.jemari.portfolio.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jemari.portfolio.Portfolio;

public class PortfolioResponse {

    private Long id;
    private Long freelancerProfileId;
    private String freelancerName;
    private String title;
    private String description;
    private String imageUrl;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PortfolioResponse() {
    }

    public PortfolioResponse(Long id, Long freelancerProfileId, String freelancerName, String title,
                             String description, String imageUrl, boolean active,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.freelancerProfileId = freelancerProfileId;
        this.freelancerName = freelancerName;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PortfolioResponse fromEntity(Portfolio portfolio) {
        String name = "";
        Long profileId = null;
        if (portfolio.getFreelancerProfile() != null) {
            profileId = portfolio.getFreelancerProfile().getId();
            name = portfolio.getFreelancerProfile().getFullName();
        }

        return new PortfolioResponse(
                portfolio.getId(),
                profileId,
                name,
                portfolio.getTitle(),
                portfolio.getDescription(),
                portfolio.getImageUrl(),
                portfolio.isActive(),
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFreelancerProfileId() {
        return freelancerProfileId;
    }

    public void setFreelancerProfileId(Long freelancerProfileId) {
        this.freelancerProfileId = freelancerProfileId;
    }

    public String getFreelancerName() {
        return freelancerName;
    }

    public void setFreelancerName(String freelancerName) {
        this.freelancerName = freelancerName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("judul")
    public String getJudul() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("deskripsi")
    public String getDeskripsi() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @JsonProperty("gambarUrl")
    public String getGambarUrl() {
        return imageUrl;
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
}
