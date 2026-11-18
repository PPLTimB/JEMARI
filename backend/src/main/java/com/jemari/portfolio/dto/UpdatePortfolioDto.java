package com.jemari.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePortfolioDto {

    @NotBlank(message = "Judul karya wajib diisi")
    @Size(max = 150, message = "Judul karya maksimal 150 karakter")
    @JsonAlias({"judul"})
    private String title;

    @JsonAlias({"deskripsi"})
    private String description;

    @NotBlank(message = "Berkas gambar atau tautan gambar karya wajib diisi")
    @JsonAlias({"gambarUrl", "image"})
    private String imageUrl;

    public UpdatePortfolioDto() {
    }

    public UpdatePortfolioDto(String title, String description, String imageUrl) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
