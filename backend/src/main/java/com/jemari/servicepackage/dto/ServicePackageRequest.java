package com.jemari.servicepackage.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ServicePackageRequest {

    @NotBlank(message = "Judul paket jasa wajib diisi")
    @Size(max = 150, message = "Judul maksimal 150 karakter")
    private String title;

    @NotBlank(message = "Kategori paket jasa wajib diisi")
    @Size(max = 100, message = "Kategori maksimal 100 karakter")
    private String category;

    private String description;

    @NotNull(message = "Harga dasar wajib diisi")
    @DecimalMin(value = "0.0", inclusive = false, message = "Harga dasar harus lebih dari 0")
    private BigDecimal basePrice;

    public ServicePackageRequest() {
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
}