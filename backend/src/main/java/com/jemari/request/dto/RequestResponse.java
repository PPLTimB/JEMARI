package com.jemari.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jemari.request.RequestStatus;
import com.jemari.request.ServiceRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RequestResponse {

    private Long id;
    private Long umkmProfileId;
    private String businessName;
    private String title;
    private String category;
    private String description;
    private BigDecimal budget;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;
    private RequestStatus status;
    private String statusLabel;
    private boolean active;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // Tambahan helper field agar cocok dengan frontend RequestDetail.vue
    private String judul;
    private String kategori;
    private String deskripsi;
    private BigDecimal harga;

    public RequestResponse() {
    }

    public static RequestResponse fromEntity(ServiceRequest entity) {
        RequestResponse dto = new RequestResponse();
        dto.setId(entity.getId());
        if (entity.getUmkmProfile() != null) {
            dto.setUmkmProfileId(entity.getUmkmProfile().getId());
            dto.setBusinessName(entity.getUmkmProfile().getBusinessName());
        }
        dto.setTitle(entity.getTitle());
        dto.setCategory(entity.getCategory());
        dto.setDescription(entity.getDescription());
        dto.setBudget(entity.getBudget());
        dto.setDeadline(entity.getDeadline());
        dto.setStatus(entity.getStatus());
        dto.setStatusLabel(resolveStatusLabel(entity.getStatus()));
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Alias field
        dto.setJudul(entity.getTitle());
        dto.setKategori(entity.getCategory());
        dto.setDeskripsi(entity.getDescription());
        dto.setHarga(entity.getBudget());
        return dto;
    }

    private static String resolveStatusLabel(RequestStatus status) {
        if (status == null) return "-";
        return switch (status) {
            case OPEN -> "Dibuka";
            case IN_PROGRESS -> "Diproses";
            case COMPLETED -> "Selesai";
            case CLOSED -> "Ditutup";
        };
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUmkmProfileId() {
        return umkmProfileId;
    }

    public void setUmkmProfileId(Long umkmProfileId) {
        this.umkmProfileId = umkmProfileId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
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

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public BigDecimal getHarga() {
        return harga;
    }

    public void setHarga(BigDecimal harga) {
        this.harga = harga;
    }
}
