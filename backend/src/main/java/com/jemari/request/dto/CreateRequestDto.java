package com.jemari.request.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateRequestDto {

    @NotBlank(message = "Judul request tidak boleh kosong")
    @Size(max = 255, message = "Judul maksimal 255 karakter")
    @JsonAlias({"judul", "title"})
    private String title;

    @NotBlank(message = "Kategori jasa tidak boleh kosong")
    @Size(max = 100, message = "Kategori maksimal 100 karakter")
    @JsonAlias({"kategori", "category"})
    private String category;

    @NotBlank(message = "Deskripsi kebutuhan tidak boleh kosong")
    @JsonAlias({"deskripsi", "description"})
    private String description;

    @NotNull(message = "Anggaran tidak boleh kosong")
    @DecimalMin(value = "1.00", message = "Anggaran harus lebih besar dari 0")
    @JsonAlias({"harga", "budget"})
    private BigDecimal budget;

    @NotNull(message = "Tanggal deadline tidak boleh kosong")
    @FutureOrPresent(message = "Tanggal deadline tidak boleh tanggal lampau")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;

    public CreateRequestDto() {
    }

    public CreateRequestDto(String title, String category, String description, BigDecimal budget, LocalDate deadline) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.budget = budget;
        this.deadline = deadline;
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
}
