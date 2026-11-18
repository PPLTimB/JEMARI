package com.jemari.catalog.variant.dto;

/**
 * DTO ringkasan estimasi durasi pengerjaan (durasi tercepat dan terlama) untuk sisi UMKM.
 */
public record EstimasiDurasiResponse(
    Integer durasiTercepat,
    Integer durasiTerlama
) {
}
