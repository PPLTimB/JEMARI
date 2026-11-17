package com.jemari.servicepackage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jemari.servicepackage.entity.ServicePackage;

public class ServicePackageResponse {

    private Long id;
    private Long freelancerId;
    private String title;
    private String category;
    private String description;
    private BigDecimal basePrice;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ServicePackageResponse() {
    }

    public static ServicePackageResponse fromEntity(ServicePackage entity) {
        ServicePackageResponse res = new ServicePackageResponse();
        res.setId(entity.getId());
        res.setFreelancerId(entity.getFreelancerId());
        res.setTitle(entity.getTitle());
        res.setCategory(entity.getCategory());
        res.setDescription(entity.getDescription());
        res.setBasePrice(entity.getBasePrice());
        res.setIsActive(entity.getIsActive());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        return res;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}   