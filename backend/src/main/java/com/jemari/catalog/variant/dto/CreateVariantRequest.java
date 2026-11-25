package com.jemari.catalog.variant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO request untuk penambahan varian paket baru oleh Freelancer.
 */
public record CreateVariantRequest(
    @NotBlank(message = "Nama varian tidak boleh kosong")
    @Size(max = 100, message = "Nama varian maksimal 100 karakter")
    String name,

    @Size(max = 255, message = "Deskripsi varian maksimal 255 karakter")
    String description,

    @NotNull(message = "Harga varian tidak boleh kosong")
    @DecimalMin(value = "0.01", message = "Harga varian harus lebih dari 0")
    @Digits(integer = 10, fraction = 2, message = "Format harga tidak valid (maksimal 10 digit angka dan 2 desimal)")
    BigDecimal price,

    @NotNull(message = "Estimasi durasi pengerjaan tidak boleh kosong")
    @Min(value = 1, message = "Estimasi durasi minimal 1 hari")
    @Max(value = 365, message = "Estimasi durasi maksimal 365 hari")
    Integer estimatedDays
) {
}
